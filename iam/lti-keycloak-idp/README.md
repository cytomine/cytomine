# Keycloak LTI Identity Provider (scaffold) — LTI 1.1 + LTI 1.3

Lets an LMS launch a Keycloak-protected tool via **either** LTI 1.1 (legacy,
OAuth 1.0a-signed direct POST) or **LTI 1.3** (OIDC-based), each showing up
as its own selectable type in Admin Console -> Identity Providers.

**Status: scaffold, not production-hardened.** It compiles against the
Keycloak SPI shape as of the ~24-25.x series, but you must verify method
signatures against your exact Keycloak version (SPI internals do shift
between releases), and close the gaps called out in "Before going to
production" below.

The two versions are architecturally very different, so they're implemented
as two independent provider types that happen to share this codebase - a
platform is either registered as `lti-1p1` or `lti-1p3`, never both.

## What's in here

### LTI 1.3 (OIDC-based)

| File                                          | Role                                                                                                  |
|-----------------------------------------------|-------------------------------------------------------------------------------------------------------|
| `LTIIdentityProviderFactory`                  | Registers "LTI 1.3" in the admin console's Add-IdP dropdown + config form                             |
| `LTIIdentityProviderConfig`                   | Typed config: platform issuer, tool client_id, JWKS URL, etc.                                         |
| `LTIIdentityProvider`                         | Builds the redirect to the LMS; validates the returned launch JWT; builds a `BrokeredIdentityContext` |
| `LTIJwtValidator`                             | Fetches/caches the platform's JWKS, verifies the launch JWT's signature                               |
| `LTILaunchHint`                               | Packs login_hint/message_hint/target_link_uri through the standard OIDC `login_hint` param            |
| `LTILoginInitiationResourceProvider(Factory)` | The `/realms/{realm}/lti/login-init` endpoint the LMS calls first                                     |
| `LTIUserRoleMapper`                           | Example: grants a client role when for all LTI standard roles                                         |

### LTI 1.1 (OAuth 1.0a-based)

| File | Role |
|---|---|
| `LTI11IdentityProviderFactory` | Registers "LTI 1.1" in the admin console's Add-IdP dropdown + config form |
| `LTI11IdentityProviderConfig` | Typed config: consumer key, shared secret, fixed tool redirect URI |
| `OAuth1LaunchValidator` | Dependency-free OAuth 1.0a HMAC-SHA1 signature verification (RFC 5849) |
| `LTI11LaunchResourceProvider(Factory)` | The `/realms/{realm}/lti11/launch` endpoint - this **is** the Launch URL you give the LMS; verifies the signed POST directly |
| `LTI11IdentityProvider` | Hands a pre-verified, one-time launch ticket into Keycloak's normal broker flow |

## Why the two flows look different internally

- **LTI 1.3** is redirect-based: the LMS calls a login-init URL, Keycloak
  redirects the browser to the LMS's authorization endpoint, the LMS
  redirects back with a signed `id_token`. `performLogin()` builds that
  first redirect; the callback endpoint verifies the returned JWT against
  the platform's JWKS.
- **LTI 1.1** has no such round trip - the LMS POSTs a directly-signed
  launch straight to a URL you gave it (the Launch URL), with no redirect
  involved at all. So `LTI11LaunchResourceProvider` verifies the OAuth 1.0a
  signature **synchronously, right there**, stashes the validated claims
  under a short-lived one-time ticket (via Keycloak's `singleUseObjects()`,
  which is cluster-safe), and only then sends the browser into Keycloak's
  broker flow (`kc_idp_hint`) carrying just the ticket ID. `performLogin()`
  forwards that ticket to the provider's own callback endpoint, which
  redeems it (single use - it's deleted on read) and builds the
  `BrokeredIdentityContext`. Both versions end up going through the same
  first-broker-login / user create-or-link / token issuance path either way.

## Build

```bash
# Edit pom.xml's <keycloak.version> to match your deployment first.
mvn clean package
# -> target/lti-identity-provider.jar
```

Deploy by copying the jar into Keycloak's `providers/` directory, then:

```bash
# Quarkus-distribution Keycloak:
bin/kc.sh build
bin/kc.sh start   # or start-dev
```

## Register your tool with the LMS

### If the LMS supports LTI 1.3

- **OIDC Login Initiation URL**: `https://<keycloak-host>/realms/<realm>/lti/login-init`
- **Target Link URI**: wherever your tool actually lives (per-link or default)
- **Redirect URI(s)**: same as target link URI (or your tool's dedicated callback), and it must match a **Valid Redirect URI** on the Keycloak client below
- **JWKS URL**: your tool's own public key set (needed if the LMS requires you to sign anything back, e.g. for grade passback / Names and Roles - not required for a plain launch)
- **Tool client_id**: assign your own value; you'll enter it in the Keycloak IdP config as "Tool Client ID"

The LMS will give you in return: its issuer, its authorization endpoint,
and its JWKS URL - these go into the Keycloak IdP config below.

### If the LMS only supports LTI 1.1

- **Launch URL**: `https://<keycloak-host>/realms/<realm>/lti11/launch`
- **Consumer Key** and **Shared Secret**: generate a strong random secret
  yourself (this is the entire trust boundary for 1.1 - treat it like a
  password), give both to whoever configures the tool in the LMS

No JWKS, no issuer, no redirect handshake - just those two values and the
Launch URL.

## Configure in Keycloak

1. Create (or reuse) the Keycloak **client** representing your tool, with a
   Valid Redirect URI matching what you registered as the target/redirect
   link above (for 1.3) or the fixed Tool Redirect URI (for 1.1).
2. Admin Console -> Identity Providers -> Add provider -> **LTI 1.3** or
   **LTI 1.1**, depending on the platform, alias e.g. `lti-acme-lms`, and
   fill in the fields from the registration step above.
3. Optionally add a role/context mapper (see `LTIUserRoleMapper` for
   the 1.3 pattern; a 1.1 equivalent would read `LTI11_ROLES`, which is
   already a split `String[]`, instead of a JSON claim).

## Before going to production

This scaffold intentionally leaves out several things you should add:

**LTI 1.3:**
- **Nonce replay protection.** `performLogin()` generates a nonce and
  stashes it on the auth session, but `validateAndExtract()` doesn't check
  it yet — add a check (and reject reused/expired nonces, e.g. via
  `session.singleUseObjects()`).
- **`exp`/`iat` validation** on the launch JWT (with reasonable clock skew).
- **`state` round-trip validation** — confirm the `state` posted back
  matches what was sent, to prevent CSRF on the callback.
- **Clustered JWKS caching.** `LTIJwtValidator`'s cache is a static
  in-process map; back it with Keycloak's Infinispan cache (or similar) so
  it's consistent and doesn't refetch per-node.
- **Deep linking support**, if you need instructors to pick/configure
  content from within the LMS — that's a second LTI message type
  (`LtiDeepLinkingRequest`) this scaffold rejects outright.

**LTI 1.1:**
- **Timestamp/nonce replay protection.** OAuth 1.0a launches include
  `oauth_timestamp`/`oauth_nonce`, which this scaffold doesn't check yet -
  add a freshness window and a nonce store (`singleUseObjects()` again
  works well here) to reject replayed launches.
- **Exact-URL sensitivity.** OAuth 1.0a signs the literal request URL - if
  Keycloak is behind a reverse proxy, misconfigured `KC_HOSTNAME`/proxy
  headers will make every signature "invalid" even with the right secret.
  Verify `uriInfo.getRequestUri()` matches the Launch URL you gave the LMS
  byte-for-byte before assuming the secret is wrong.

**Both:**
- **Rate limiting / logging** on the public launch endpoints, since both
  are unauthenticated by necessity.

## Multiple LMS platforms

Add one Identity Provider instance (alias) per platform - each with its own
config - same as registering multiple SAML IdPs today. A 1.1 LMS gets an
`lti-1p1` instance, a 1.3 LMS gets an `lti-1p3` instance; both resource
endpoints resolve the right one by matching consumer key / issuer from the
incoming request.

