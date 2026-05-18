# Diffy
[![Project status](https://img.shields.io/badge/status-active-brightgreen.svg)](#status)
[![Docker](https://img.shields.io/docker/pulls/diffy/diffy)](https://hub.docker.com/r/diffy/diffy)
[![Downloads](https://img.shields.io/github/downloads/opendiffy/diffy/total.svg)](https://github.com/opendiffy/diffy/releases/latest)
[![License: CC](https://img.shields.io/badge/License-CC%20BY%20NC%20ND-blue.svg)](https://creativecommons.org/licenses/by-nc-nd/4.0/legalcode)

[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_macos_latest.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_macos_latest.yml)
[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_macos_15.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_macos_15.yml)
[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_macos_14.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_macos_14.yml)

[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_windows_latest.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_windows_latest.yml)
[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_windows_2025.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_windows_2025.yml)
[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_windows_2022.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_windows_2022.yml)

[![Build status](https://github.com/opendiffy/diffy/actions/workflows/maven_ubuntu_latest.yml/badge.svg)](https://github.com/opendiffy/diffy/actions/workflows/maven_ubuntu_latest.yml)

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/diffy)

## Status

Diffy is used in production at:
* [Mixpanel](https://engineering.mixpanel.com/safely-rewriting-mixpanels-highest-throughput-service-in-golang-mixpanel-engineering-62cd69b5ebdb)
* Airbnb [(Scalabity)](https://www.infoq.com/presentations/airbnb-services-scalability/) [(Migration)](https://www.infoq.com/presentations/airbnb-soa-migration/)
* [Twitter](https://blog.twitter.com/engineering/en_us/a/2015/diffy-testing-services-without-writing-tests.html)
* Baidu
* Bytedance

and blogged about by cloud infrastructure providers like:
* [Microsoft](https://microsoft.github.io/code-with-engineering-playbook/automated-testing/shadow-testing/)
* [Google](https://cloud.google.com/architecture/application-deployment-and-testing-strategies#shadow_test_pattern)
* [Alibaba Cloud](https://www.alibabacloud.com/blog/traffic-management-with-istio-3-traffic-comparison-analysis-based-on-istio_594545)
* [KrakenD](https://www.krakend.io/blog/migrate-aws-api-gateway-to-krakend/)
* [Datawire](https://blog.getambassador.io/next-level-testing-with-an-api-gateway-and-continuous-delivery-9cbb9c4564b5)

Visit our [engineering blog](https://content.sn126.com/blog) to keep up with the latest developments in Diffy.

Diffy is being actively developed and maintained by the engineering team at [Sn126](https://www.sn126.com).

Feel free to contact us via [discord](https://discord.gg/QEJRxgVfD8), [linkedin](https://www.linkedin.com/company/diffy), or [twitter](https://twitter.com/diffyproject).

## What is Diffy?

Diffy finds regressions in your service by running your new code and your old
code side by side and comparing what they return. It behaves as a proxy that
multicasts every request it receives to each running instance, compares the
responses, and renders a verdict — **Safe to ship** when the candidate matches
the primary within noise tolerance, or **Regressions detected** when real
differences surface.

The premise is simple: if two implementations of a service return “similar”
responses for a sufficiently large and diverse set of requests, the newer
implementation can be treated as regression-free.

## How does Diffy work?

Diffy acts as a proxy that accepts requests drawn from any source you provide
and multicasts each of those requests to three different service instances:

1. A **candidate** instance running your new code
2. A **primary** instance running your last known-good code
3. A **secondary** instance running the same known-good code as the primary

When those services respond, Diffy compares the responses and looks for two things:

1. **Real differences** observed between the candidate and the primary.
2. **Non-deterministic noise** observed between the primary and the secondary.
   Since both run known-good code, you should expect them to agree. Where they
   don’t, your service is exhibiting non-deterministic behavior — Diffy treats
   that signal as noise.

![Diffy Topology](/images/diffy_topology.png)

Diffy measures how often primary and secondary disagree with each other vs.
how often primary and candidate disagree. If those rates are comparable,
Diffy concludes the candidate has no real regression and the difference is
just noise — which is what the **signal-to-noise** indicator in the UI
summarizes at a glance.

## The Diffy UI

Diffy ships with a single-page UI organized around five views:

* **Overview** — the run verdict (*Safe to ship* / *Regressions detected*),
  signal-to-noise, traffic and diff timeseries, and the top failing endpoints.
* **Endpoints** — drill into any endpoint, walk the field tree, and open
  side-by-side or three-way diffs in the inspector.
* **Noise** — review which fields are flagged as noise and tune cancellation
  rules per endpoint.
* **Transformations** — define request/response transformations applied
  before comparison.
* **Runs** — browse the history of past comparison runs and switch between
  them.

![Diffy UI](/images/diffy-ui.png)

## Documentation

Detailed Diffy Documentation is available [here](https://content.sn126.com/docs/diffy).

### Support
Please reach out to isotope@sn126.com for support. We look forward to hearing from you.

### Code of Conduct
1. Bug reports are welcome even if submitted anonymously via fresh github accounts.
2. Anonymous feature and support requests will be ignored.

## License

    Copyright (C) 2019 Sn126, Inc.

    This license allows reusers to copy and distribute the material in 
    any medium or format in unadapted form only, for noncommercial purposes 
    only, and only so long as attribution is given to the creator. 

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International Public License
    for more details.

    You should have received a copy of the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0
    International Public License along with this program. If not, see 
    https://creativecommons.org/licenses/by-nc-nd/4.0/.
