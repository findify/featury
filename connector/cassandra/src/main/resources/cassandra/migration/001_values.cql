create table if not exists features(
  ns text,
  scope text,
  tenant text,
  name text,
  id text,
  values blob,
  primary key ((ns, scope, tenant, id), name)
);