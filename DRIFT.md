# Operations and Deployment - Read This First

This guide is primarily intended for the team's operations/deployment lead, but everyone on the team should be familiar with its contents.

## Running Locally

Requirements: Docker Desktop (Windows/Mac) or Docker Engine (Linux). On Windows, Docker Desktop requires WSL2. See the common issues below.

```bash
cd infra
docker compose up --build
```

The app will then be available at `http://localhost:PORT`. The applicable port for your case is specified in `infra/docker-compose.override.yml`. Login credentials for the seed data are provided in the README.

* Stop: `Ctrl+C`, or `docker compose down`
* Start with an empty database: `docker compose down -v` and then run `up --build` again

## Common Local Issues

* **"WSL 2 installation is incomplete" (Windows):** Run `wsl --install` in PowerShell as Administrator, restart the computer, and start Docker Desktop again.
* **"port is already allocated":** Another process is using the port. Stop it, or change the port in `infra/docker-compose.override.yml` (you are allowed to modify this file).
* **Code changes are not visible:** You forgot to use `--build`.
* **Database is in an unexpected state:** Run `docker compose down -v` and start again. The volume is local only; nothing is lost in the production environment.

## How Deployment Works

* A push to `develop` rebuilds your staging environment, while a push to `main` rebuilds production. The addresses are provided in the README.
* A green checkmark or red X on the commit in GitHub shows whether the deployment succeeded. If you see a red X, click the indicator and read the build log.
* **A failed build does not bring down your environment.** The last successfully deployed version continues running until a new build succeeds.
* The build takes a few minutes. Near the deadline, all teams push at the same time and the queue becomes longer, so push well in advance.
* Workflow: always test on `develop` before merging into `main`.

## The Platform Contract - Four Rules

Your deployment environment requires the following:

1. `infra/docker-compose.yml` must remain in its current location.
2. The web service must be named `app`.
3. `expose` must be used in `infra/docker-compose.yml`, never `ports` (local ports belong in `docker-compose.override.yml`).
4. The app's actual listening port must match the `expose` value.

An automated check runs on every push and will show a red X with an explanation if rules 1-3 are violated. Rule 4 cannot be checked automatically: if you change the port the app listens on, update the `expose` value at the same time.

Everything else is up to you: upgrade the language version, framework, Docker base images, add services to the Compose configuration, and so on.

## Changing the Postgres Version (v2 Requirement)

The database volume in your deployment environment contains data files from the current Postgres version. If you only change the image version, the database will not start. Follow these steps:

1. Test locally first (`docker compose down -v` gives you a fresh local volume).
2. Submit a tech support ticket (category **"Deploy & CI"**) **BEFORE** pushing the version change to `develop`/`main`, and state that you need a database reset for the Postgres version change.
3. We will reset the volume in the deployment environment as part of your deployment.

## Resetting the Database in the Deployment Environment

You cannot reset the database yourself in staging/production. Submit a tech support ticket:

https://chas-challenge.comerit.se/support/

## Frontend in v2

Build the frontend in the same Dockerfile as the backend (using a separate build stage that copies the build output into the backend image). A separate frontend container will not receive its own public address.
