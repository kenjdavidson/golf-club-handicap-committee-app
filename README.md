# golf-club-handicap-committee-app
Desktop application used by Handicap Committees to verify members

Includes the Golf Canada SSL certificate at `src/main/resources/certs/golfcanada.pem`, loaded into the application's SSL trust configuration during startup.

To run the application locally, provide login credentials before startup:

```bash
export APP_AUTH_USERNAME=committee
export APP_AUTH_PASSWORD=committee
./gradlew bootRun
```
