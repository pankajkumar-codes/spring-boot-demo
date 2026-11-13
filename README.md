# spring-boot-demo
## API

Base path `/api/v1/software-engineers`: `GET` (optional `page`, `size`), `GET /{id}`, `GET /search?name=`, `GET /filter?techStack=`, `POST`, `PUT /{id}`, `DELETE /{id}`.
Bodies are `{"name": "...", "techStack": "..."}` (both required, 1 to 100 chars). Bad input returns 400, an unknown id returns 404, `POST` returns 201 with a `Location` header.
The machine-readable description is in `docs/openapi.json`.

## Testing it with api-test-pilot

```
python <skill>/scripts/intake.py docs/openapi.json -o intake.json
python <skill>/scripts/gen_pytest.py intake.json -o api_tests --types all
APITEST_BASE_URL=http://localhost:8080 APITEST_ALLOW_DESTRUCTIVE=yes APITEST_PATH_ID=1 python -m pytest api_tests -q -ra
```
Run it against a throwaway database: the tests create, change and delete rows and send hostile input.
