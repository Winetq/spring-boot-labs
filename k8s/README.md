# Kubernetes (local, minikube)

This folder contains the Kubernetes configuration to run the whole project **locally on
[minikube](https://minikube.sigs.k8s.io/)**. It mirrors the `docker/docker-compose.yml` setup: the two
microservices (coach, swimmer), their PostgreSQL databases, RabbitMQ, the Spring Cloud Gateway and the static
frontend — each as its own Deployment + Service.

> This is a learning/local setup only. The production-grade AWS deployment lives in `infrastructure/` (AWS CDK).
> The databases run as plain Deployments (no persistence yet) — data is lost when a pod restarts.

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
host browser, so we use `kubectl port-forward` to bind stable `localhost` ports (see below). Because of this,
`configuration.js` (backend URL), the gateway `.host()` predicate and the CORS origin all use `localhost`.

## Run

```bash
# 1. start the cluster
minikube start

# 2. apply everything (-R also descends into shared/)
kubectl apply -R -f k8s/

# 3. wait until all pods are Running (a service may restart once while the DB starts — that's fine)
kubectl get pods -w

# 4. expose the frontend and gateway on stable localhost ports (each blocks — use two terminals)
kubectl port-forward svc/frontend-service 30000:80
kubectl port-forward svc/gateway-service 30080:8080
```

Then open **http://localhost:30000** in the browser and log in via Okta.

## Useful commands

```bash
# overview of everything
kubectl get all

# watch pod status live
kubectl get pods -w

# live application logs (follow)
kubectl logs -f deploy/coach-deployment
kubectl logs -f deploy/gateway-deployment

# logs from a previous (crashed) container
kubectl logs <pod> --previous

# describe a pod (events: image pull, mount, OOM, scheduling...)
kubectl describe pod <pod>

# shell into a container
kubectl exec -it <pod> -- sh

# verify the mounted configuration.js actually overrode the image file
kubectl exec deploy/frontend-deployment -- cat /usr/local/apache2/htdocs/js/configuration.js

# re-apply after editing a manifest (apply is idempotent)
kubectl apply -f k8s/frontend.yaml

# a ConfigMap mounted via subPath does NOT hot-reload — restart the pod after editing it
kubectl rollout restart deploy/frontend-deployment

# tear everything down
kubectl delete -R -f k8s/
minikube stop
```
