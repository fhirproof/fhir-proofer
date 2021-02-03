
## FHIR Proofer

FHIR Proofer aims to provide a simple way to test or proofing out interacting with a FHIR store. The FHIR Proof store's goal is to be as FHIR compliant as possible while still remaining a slim product.

### FHIR Versions
FHIR Proofer only supports the FHIR R4 specification at this time.

### Usage
To utilize the FHIR Proofer simply add a dependency to your project and then use your desired testing framework(s) to substitute calls to a remote FHIR server to the FHIR Proofer stores. After executing application code the FHIR Proofer store can then be queried within tests to verify the outcome of application code.
