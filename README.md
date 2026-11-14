# spring-boot-demo
## Configuration

Database settings come from environment variables. The defaults are for local development only; set your own values anywhere else.

| Variable | Default | Used by |
|---|---|---|
| `DB_HOST` | `localhost` | app |
| `DB_PORT` | `5332` | app and docker-compose (host port) |
| `DB_NAME` | `software-engineers` | app and docker-compose |
| `DB_USER` | `software-engineers` | app and docker-compose |
| `DB_PASSWORD` | `password` | app and docker-compose |

```
export DB_PASSWORD='choose-a-password'
docker compose up -d
./mvnw spring-boot:run
```
Do not commit real credentials. Change the password before the first `docker compose up`, because the volume keeps the one it was created with.

### Rate limit

Each client (TCP peer address; `X-Forwarded-For` is ignored because it can be forged) gets a token bucket: `RATE_LIMIT_RPS` tokens per second (default `50`) up to `RATE_LIMIT_BURST` (default `20`). Over the limit the API answers `429` with `Retry-After: 1`. `RATE_LIMIT_ENABLED=false` turns it off. Behind a reverse proxy every request has the proxy's address, so limit at the proxy instead.

## API

Base path `/api/v1/software-engineers`: `GET` (optional `page`, `size`), `GET /{id}`, `GET /search?name=`, `GET /filter?techStack=`, `POST`, `PUT /{id}`, `DELETE /{id}`.
Bodies are `{"name": "...", "techStack": "..."}` (both required, 1 to 100 chars). Bad input returns 400, an unknown id returns 404, too many requests from one client return 429, `POST` returns 201 with a `Location` header.
The machine-readable description is in `docs/openapi.json`.

## Testing it with api-test-pilot

```
python <skill>/scripts/intake.py docs/openapi.json -o intake.json
python <skill>/scripts/gen_pytest.py intake.json -o api_tests --types all
APITEST_BASE_URL=http://localhost:8080 APITEST_ALLOW_DESTRUCTIVE=yes APITEST_PATH_ID=1 python -m pytest api_tests -q -ra
```
Run it against a throwaway database: the tests create, change and delete rows and send hostile input.
