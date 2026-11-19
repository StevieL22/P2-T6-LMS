# frontend/

React single-page app for Admin, Trainer and Learner roles.

## Layout
```
src/
  app/          router, providers, app shell
  components/   shared UI components
  features/     auth | courses | content | enrolments | assessments | reviews | notifications | dashboards
  services/     API client, JWT interceptor
  hooks/        reusable hooks
  layouts/      public, learner, trainer, admin
  routes/       protected, role-aware routes
  tests/        test utilities and E2E specs
```