# Kubernetes (local, minikube)

This folder contains the Kubernetes configuration to run the whole project **locally on
[minikube](https://minikube.sigs.k8s.io/)**. It mirrors the `docker/docker-compose.yml` setup: the two
microservices (coach, swimmer), their PostgreSQL databases, RabbitMQ, the Spring Cloud Gateway and the static
frontend - each as its own Deployment + Service.

> This is a learning/local setup only. The production-grade AWS deployment lives in `infrastructure/` (AWS CDK).
> The databases run as plain Deployments (no persistence yet) - data is lost when a pod restarts.

## Layout

| File | Purpose |
|------|---------|
| `coach.yaml`, `swimmer.yaml` | microservice Deployment + Service |
| `gateway.yaml` | Spring Cloud Gateway (Deployment + NodePort Service) |
| `frontend.yaml` | Apache static frontend (Deployment + NodePort Service) |
| `frontend-config.yaml` | ConfigMap holding `configuration.js` (backend URL), mounted over the image file |
| `postgres-coach.yaml`, `postgres-swimmer.yaml` | one PostgreSQL Deployment + Service per service |
| `shared/postgres-config.yaml`, `shared/postgres-secret.yaml` | shared DB config / credentials |
| `shared/rabbit.yaml`, `shared/rabbit-config.yaml`, `shared/rabbit-secret.yaml` | RabbitMQ broker + config / credentials |

## Networking model

Requests fall into two categories:

- **pod → pod** (e.g. gateway → coach/swimmer, service → RabbitMQ/Postgres): use the internal **Service DNS**
  name (`coach-service:8081`, `rabbit-service:5672`, ...).
- **browser → cluster** (the frontend loading, and its calls to the gateway): must use a **host-reachable**
  address, not internal DNS. Exposed via **NodePort** (frontend `30000`, gateway `30080`).

On **macOS with the Docker driver** the minikube node IP (`minikube ip`) is usually **not reachable** from the
host browser, so it's required to use `kubectl port-forward` to bind stable `localhost` ports (see below). Because of this,
`configuration.js` (backend URL), the gateway `.host()` predicate and the CORS origin all use `localhost`.

## Run

```bash
# 1. start the cluster
minikube start

# 2. apply everything (-R also descends into shared/)
kubectl apply -R -f k8s/

# 3. wait until all pods are Running
kubectl get pods -w

# 4. expose the frontend and gateway on stable localhost ports (each blocks - use two terminals)
kubectl port-forward svc/frontend-service 30000:80
kubectl port-forward svc/gateway-service 30080:8080
```

Then open **http://localhost:30000** in the browser and log in via Okta.

## Resources, probes & autoscaling

`coach.yaml` and `swimmer.yaml` are configured to mirror the production ECS setup:

- **Resource requests/limits** - `requests: 256Mi / 100m`, `limits: 768Mi / 500m`. The request is the
  scheduler's reservation (and the base for the HPA CPU %); the limit is the hard ceiling (CPU over-limit =
  throttled, memory over-limit = **OOMKilled**). This puts the pods in the **Burstable** QoS class.
- **`JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=60`** - caps the JVM heap at 60% of the memory limit so the
  container leaves headroom for threads, Metaspace and off-heap buffers. Without a memory limit the JVM sizes
  the heap from the **whole node's** RAM and over-allocates.
- **`livenessProbe` / `readinessProbe`** on `/actuator/health` (named port `http`) - Liveness failure →
  kubelet **restarts** the container; readiness failure → pod is **removed from the Service endpoints** (no
  traffic) but not restarted. Both run every `periodSeconds: 10` with `timeoutSeconds: 3`, executed by the
  **kubelet** on the node (not the control plane).
- **HorizontalPodAutoscaler** (`autoscaling/v2`) - target **CPU 60%** of the request, `minReplicas: 2`,
  `maxReplicas: 4`. Requires `resources.requests.cpu` and the metrics-server addon
  (`minikube addons enable metrics-server`). Since the CPU request is `100m`, the 60% threshold is only `60m`
  per pod, so even light traffic triggers a scale-up.

```bash
# watch the autoscaler react (TARGETS shows current%/target%)
kubectl get hpa -w

# live resource usage per pod
kubectl top pods

# inspect the heap ceiling the JVM computed from the container limit (~60% of 768Mi)
kubectl exec <pod> -- sh -c 'java -XX:+PrintFlagsFinal -version 2>/dev/null | grep MaxHeapSize'
```

## Load testing (HPA in action)

The gateway was hammered with [k6](https://k6.io/) (`load-test/load-test.js`, ramping up to **100 VUs over
4 minutes**) against `/coaches` and `/swimmers` and the manifests were tuned between runs. Each run scaled the
targeted service `2 → 4` on CPU while the other stayed idle (the HPA scales **per service**, based on each
one's real load).

| Config | Fail rate | Checks OK | p95 latency | Throughput | Iterations |
|--------|-----------|-----------|-------------|------------|------------|
| **1.** `limits: 512Mi`, `minReplicas: 1` | 34.5% | 82.7% | 35.4s | 8 req/s | 1 967 |
| **2.** `limits: 768Mi`, `minReplicas: 1` | 9.2% | 95.4% | 30.0s | 19 req/s | 4 578 |
| **3.** `limits: 768Mi`, `minReplicas: 2` | **5.5%** | **97.2%** | **6.5s** | **37 req/s** | **8 996** |

Takeaways:

- **Run 1 → 2 (raise memory limit):** at `512Mi` the JVM was `OOMKilled` under load (heap + threads + Hikari +
  RabbitMQ buffers exceeded the limit). Bumping to `768Mi` cut the fail rate from 34% to 9%.
- **Run 2 → 3 (raise `minReplicas` to 2):** the biggest quality jump - **p95 dropped from 30s to 6.5s** and
  throughput doubled. With two baseline pods the initial 100-VU burst is split instead of hammering a single
  cold pod for ~60s (JVM startup) before the HPA can add replicas.
- **Remaining ~5%:** this is the **cold-start** problem, not memory - a brand-new pod needs ~60s to become
  `Ready`, so a sudden burst still overshoots briefly. Real fixes are a gentler traffic ramp, a faster startup
  (GraalVM native image / CRaC), or a higher `minReplicas` - not more memory.

## Useful commands

```bash
# overview of everything
kubectl get all

# watch pod status live
kubectl get pods -w

# live application logs (follow)
kubectl logs -f deploy/coach-deployment
kubectl logs -f deploy/gateway-deployment

# describe a pod (events: image pull, mount, OOM, scheduling...)
kubectl describe pod <pod>

# verify the mounted configuration.js actually overrode the image file
kubectl exec deploy/frontend-deployment -- cat /usr/local/apache2/htdocs/js/configuration.js

# re-apply after editing a manifest (apply is idempotent)
kubectl apply -f k8s/frontend.yaml

# a ConfigMap mounted via subPath does NOT hot-reload - restart the pod after editing it
kubectl rollout restart deploy/frontend-deployment

# tear everything down
kubectl delete -R -f k8s/
minikube stop
```

## Next steps

This setup is intentionally kept as **raw manifests** for learning. Natural follow-ups, if desired:

- **Databases as StatefulSets (PVC / persistent storage).** The PostgreSQL pods currently run as plain
  Deployments, so data is lost on restart - fine for this demo, but not how a stateful workload is run in
  practice. Converting them to StatefulSets introduces **PersistentVolumeClaims** (durable storage), a stable
  network identity (`postgres-0` + headless Service) and `volumeClaimTemplates` (one PVC per replica) - the
  storage/identity concepts the stateless services never touch.
- **Package the manifests as a Helm chart.** `coach.yaml` and `swimmer.yaml` are near-duplicates; Helm would
  collapse them into a single template under `templates/` plus per-service `values.yaml` files, removing the
  duplication and enabling per-environment values (dev/staging/prod), versioned releases and one-command
  rollbacks (`helm rollback`).
