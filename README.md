# DBaaS Aggregator API

## Overview
This documentation presents REST API for "Database as a Service" component. DBaaS is aggregator for all adapters. 
DBaaS is purposed to aggregate the requests for the administrated databases and send to the necessary adapter. 
DBaaS stores information about all databases used in cloud project. These databases are isolated by namespace. 
DBaaS uses Classifier for identification a database within one cloud namespace. Classifier indicates service information, 
such as scope, microserviceName, tenantId, namespace.

* Installation notes: [installation note.md](./docs/installation/installation.md)
* List of supported APIs: [rest-api docs](./docs/rest-api.md)
* Information about DBaaS features: https://perch.qubership.org/display/CLOUDCORE/DbaaS+Features 
