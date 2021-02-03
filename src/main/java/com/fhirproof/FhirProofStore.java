package com.fhirproof;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import ca.uhn.fhir.parser.IParser;
import org.apache.commons.lang3.ClassUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.context.SimpleWorkerContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.utils.FHIRPathEngine;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FhirProofStore is a self contained FHIR store that can be utilized in place of an external FHIR server
 * or as a slim temporary store. This in-memory store strives to be as compliant with the R4 FHIR specification
 * as possible. It also offers both access to the underlying data via both the standard FHIR interactions or directly
 * to the set of <tt>Map</tt> structures.
 *
 * It is important to note that the data within this store is volatile and the FhirProofStore should not be used for
 * any purposes other than the testing or temporary storage of data.
 *
 */
public class FhirProofStore {
    public static final String FHIR_STORE_URL = "https://fhirproof.github.io/fhir-proofer/fhir/";
    private static final FhirContext fhirContext = FhirContext.forR4();
    private static final IParser FHIR_PARSER = FhirContext.forR4().newJsonParser();

    private final FHIRPathEngine pathEngine;
    private final HashMap<String, HashMap<String, Resource>> store = new HashMap<>();
    private final HashMap<String, ISearchEvaluator> evaluators = new HashMap<>();
    private final HashMap<String, IOperationExecutor> executors = new HashMap<>();

    /**
     * Constructs an empty FHIR store with default functionality.
     * @throws FhirProofException If the underlying functionality fails to initialize
     */
    public FhirProofStore() throws FhirProofException {
        try {
            pathEngine = new FHIRPathEngine(new SimpleWorkerContext());
            pathEngine.setHostServices(new FhirProofEvaluator(this));
            resetEvaluators();
        } catch (IOException ioex) {
            throw new FhirProofException(ioex);
        }
    }

    /**
     *
     * @param hostService A custom implementation of an IEvaluationContext
     * @throws FhirProofException If the underlying functionality fails to initialize
     */
    public FhirProofStore(FHIRPathEngine.IEvaluationContext hostService) throws FhirProofException {
        try {
            pathEngine = new FHIRPathEngine(new SimpleWorkerContext());
            pathEngine.setHostServices(hostService);
            resetEvaluators();
        } catch (IOException ioex) {
            throw new FhirProofException(ioex);
        }
    }

    /**
     * The raw <tt>Map</tt> of the FHIR store.
     * @return The raw <tt>Map</tt> of the FHIR store.
     */
    public HashMap<String, HashMap<String, Resource>> store() {
        return store;
    }

    /**
     * Adds a custom search evaluator, or overwrites it if one exists for that parameter type.
     * @param searchEvaluator Custom implementation of ISearchEvaluator
     */
    public void addEvaluator(ISearchEvaluator searchEvaluator) {
        evaluators.put(searchEvaluator.getParameterType(), searchEvaluator);
    }

    /**
     * Adds a custom operation executor, or overwrites it if one exists for the same name.
     * @param operationExecutor Custom implementation of IOperationExecutor
     */
    public void addExecutor(IOperationExecutor operationExecutor) {
        executors.put(operationExecutor.getOperationName(), operationExecutor);
    }

    /**
     * Rests all the search evaluators to the default versions.
     */
    public void resetEvaluators() {
        evaluators.put(DateEvaluator.PARAM_TYPE, new DateEvaluator(fhirContext));
        evaluators.put(StringEvaluator.PARAM_TYPE, new StringEvaluator(fhirContext));
        evaluators.put(NumberEvaluator.PARAM_TYPE, new NumberEvaluator(fhirContext));
        evaluators.put(TokenEvaluator.PARAM_TYPE, new TokenEvaluator(fhirContext));
        evaluators.put(ReferenceEvaluator.PARAM_TYPE, new ReferenceEvaluator(fhirContext));
    }

    /**
     * Performs a read of a FHIR resource.
     * @param resource Resource type (e.g. Patient)
     * @param id ID of the resource to read
     * @param <T> Resource type being read
     * @return The resource requested.
     * @throws FhirProofException Indicating why the read failed.
     */
    public <T> T read(String resource, String id) throws FhirProofException {
        if (!store.containsKey(resource) || !store.get(resource).containsKey(id)) {
            throw new FhirProofException(String.format("'%s/%s' not found", resource, id));
        }
        return (T) store.get(resource).get(id).copy();
    }

    /**
     * Creates a FHIR resource in the store.
     * @param resource The resource to create
     * @return The ID of the newly created resource.
     */
    public String create(Resource resource) {
        Resource copy = resource.copy();
        copy.getMeta().setVersionId(Base64.getEncoder().encodeToString(String.valueOf(Instant.now().getEpochSecond()).getBytes()));
        copy.getMeta().setLastUpdated(DateTimeType.now().getValue());
        String resourceType = copy.getResourceType().name();
        if (!store.containsKey(resourceType)) {
            store.put(resourceType, new HashMap<>());
        }

        String id = UUID.randomUUID().toString();
        copy.setId(id);
        store.get(resourceType).put(id, copy);
        return id;
    }

    /**
     * Updates the indicated FHIR resource.
     * @param resource Resource type (e.g. Patient)
     * @param content The updated version of the resource
     * @throws FhirProofException Indicating why the update failed.
     */
    public void update(String resource, String content) throws FhirProofException {

        try {
            Class clazz = Class.forName("org.hl7.fhir.r4.model." + resource);
            update(resource, (Resource) FHIR_PARSER.parseResource(clazz, content));
        } catch (Exception ex) {
            if (ex instanceof FhirProofException) {
                throw (FhirProofException) ex;
            }
            throw new FhirProofException(ex.getMessage());
        }
    }

    /**
     * Updates the indicated FHIR resource.
     * @param resource Resource type (e.g. Patient)
     * @param instance The updated version of the resource
     * @throws FhirProofException Indicating why the update failed.
     */
    public void update(String resource, Resource instance) throws FhirProofException {
        String id = instance.getIdElement().getIdPart();
        update(resource, id, instance);
    }

    /**
     * Updates the indicated FHIR resource.
     * @param resource Resource type (e.g. Patient)
     * @param id ID of the resource to update
     * @param instance The updated version of the resource
     * @throws FhirProofException Indicating why the update failed.
     */
    public void update(String resource, String id, Resource instance) throws FhirProofException {
        Resource copy = instance.copy();
        copy.getMeta().setVersionId(Base64.getEncoder().encodeToString(String.valueOf(Instant.now().getEpochSecond()).getBytes()));
        copy.getMeta().setLastUpdated(DateTimeType.now().getValue());
        if (!store.containsKey(resource) || !store.get(resource).containsKey(id)) {
            throw new FhirProofException(String.format("%s/%s not found", resource, id));
        }
        store.get(resource).put(id, copy);
    }

    /**
     * Deletes the indicated FHIR resource from the store.
     * @param resource Resource type (e.g. Patient)
     * @param id ID of the resource to delete
     * @throws FhirProofException Indicating why the delete failed.
     */
    public void delete(String resource, String id) throws FhirProofException {
        if (!store.containsKey(resource) || !store.get(resource).containsKey(id)) {
            throw new FhirProofException(String.format("%s/%s not found", resource, id));
        }
        store.get(resource).remove(id);
    }

    /**
     * Performs a search for the matching FHIR resources.
     * @param resource Resource type (e.g. Patient)
     * @param query FHIR formatted query string
     * @return A bundle containing the matching resources
     * @throws FhirProofException Indicating why the search failed.
     */
    public Bundle search(String resource, String query) throws FhirProofException {
        try {

            // if the resource set being searched on hasn't been populated then create it
            if (!store.containsKey(resource)) {
                store.put(resource, new HashMap<>());
            }

            Class clazz = Class.forName("org.hl7.fhir.r4.model." + resource);
            List<SearchParamDefinition> paramDefs =
                    Arrays.stream(clazz.getDeclaredFields())
                            .map(f -> f.getDeclaredAnnotation(SearchParamDefinition.class))
                            .filter(a -> a != null)
                            .collect(Collectors.toList());

            for (Class<?> i : ClassUtils.getAllInterfaces(clazz)) {
                for (Field f : i.getDeclaredFields()) {
                    SearchParamDefinition def = f.getDeclaredAnnotation(SearchParamDefinition.class);
                    if (def != null) {
                        paramDefs.add(def);
                    }
                }
            }

            Map<String, List<String>> params =
                    Arrays.stream(query.split("&"))
                            .collect(
                                    Collectors.groupingBy(
                                            p -> p.split("=")[0],
                                            Collectors.mapping((String p) -> p.split("=")[1], Collectors.toList())));

            // Loop through each search parameter (key) and compare to each resource to see if it
            // satisfies the condition. If it does, then add that match to a paired down match list and
            // repeat the process using the paired down match list and the next parameter.
            List<String> matches = store.get(resource).keySet().stream().collect(Collectors.toList());
            List<String> revIncludeQueries = null;
            for (String key : params.keySet()) {
                if (key.equals("_revinclude")) {
                    // Just flag that we want to do a RevInclude after we do the
                    // initial matching and then continue to skip this parameter
                    revIncludeQueries = params.get(key);
                    continue;
                }

                List<String> ands = new ArrayList<>();
                List<String> ors = new ArrayList<>();

                // Build the list(s) of conditions from the parameter structure
                for (String value : params.get(key)) {
                    if (value.contains(",")) {
                        ors.addAll(Arrays.asList(value.split(",")));
                    } else {
                        ands.add(value);
                    }
                }

                // Look for a search parameter definition attribute that defines this parameter
                Optional<SearchParamDefinition> paramDef =
                        paramDefs.stream().filter(pd -> pd.name().equals(key)).findFirst();
                String path;
                if (!paramDef.isPresent()) {
                    throw new FhirProofException(
                            String.format("No search parameter found for '%s'", key));
                } else if (paramDef.get().path().equals("")) {
                    switch (paramDef.get().name()) {
                        case "_id":
                            path = String.format("%s.id", resource);
                            break;
                        default:
                            throw new FhirProofException(
                                    String.format(
                                            "Universal parameter of '%s' is not supported", paramDef.get().name()));
                    }
                } else {
                    path = paramDef.get().path();
                }

                List<String> workingMatches = new ArrayList<>();
                for (String id : matches) {
                    // Use a FHIR Path evaluation engine to extract the actual data field for the resource
                    List<Base> base = pathEngine.evaluate(store.get(resource).get(id), path);
                    if (!evaluators.containsKey(paramDef.get().type())) {
                        throw new FHIRException(
                                String.format(
                                        "No SearchEvaluator defined for '%s' searches", paramDef.get().type()));
                    }
                    // get the ISearchEvaluator for this type of parameter and call the evaluate method
                    if (evaluators.get(paramDef.get().type()).evaluate(base, ands, ors)) {
                        workingMatches.add(id);
                    }
                }
                matches = workingMatches;
            }

            Bundle bundle = new Bundle();
            for (String id : matches) {
                // Safety check to prevent duplicate primary matches
                if (bundle.getEntry().stream().noneMatch(e -> e.getResource().getIdElement().getIdPart().equals(id))) {
                    bundle.addEntry().setResource(store.get(resource).get(id).copy());
                }
            }

            if (revIncludeQueries != null) {
                for (String id : matches) {
                    for (String revInclude : revIncludeQueries) {
                        String[] parts = revInclude.split(":");
                        if (store().containsKey(parts[0])) {
                            Bundle revSearch = search(parts[0], String.format("%s=%s/%s", parts[1], resource, id));
                            revSearch.getEntry().forEach(e -> {
                                // Check if we've already added this _revinclude hit to prevent duplicates
                                // NOTE: you cannot use the default distinct, which uses the Object.equals(), since
                                //      a copy is performed on the hits specifically to ensure that the returned
                                //      resources refer to a unique reference, thus isolating the stored version
                                //      from accidental changes
                                if (bundle.getEntry().stream().noneMatch(existing -> existing.getResource().getId().equals(e.getResource().getId()))) {
                                    bundle.addEntry().setResource(e.getResource());
                                }
                            });
                        }
                    }
                }
            }

            bundle.setTotal(bundle.getEntry().size());
            return bundle;
        } catch (Exception ex) {
            throw new FhirProofException(ex.getMessage());
        }
    }

    /**
     * Performs a conditional create of a FHIR resource.
     * @param type Resource type (e.g. Patient)
     * @param resource The resource to create
     * @param ifNoneExistsQuery If-None-Exists FHIR query
     * @return ConditionalCreateResponse with the ID and status code of the request
     * @throws FhirProofException Indicating why the conditional create failed.
     */
    public ConditionalCreateResponse conditionalCreate(String type, String resource, String ifNoneExistsQuery) throws FhirProofException {
        try {
            Class clazz = Class.forName("org.hl7.fhir.r4.model." + resource);
            return conditionalCreate((Resource) FHIR_PARSER.parseResource(clazz, resource), ifNoneExistsQuery);
        } catch (Exception ex) {
            if (ex instanceof FhirProofException) {
                throw (FhirProofException) ex;
            }
            throw new FhirProofException(ex.getMessage());
        }
    }

    /**
     * Performs a conditional create of a FHIR resource.
     * @param resource The resource to create
     * @param ifNoneExistsQuery If-None-Exists FHIR query
     * @return ConditionalCreateResponse with the ID and status code of the request
     */
    public ConditionalCreateResponse conditionalCreate(Resource resource, String ifNoneExistsQuery) {
        try {
            String type = resource.getResourceType().name();
            String id;
            int status;
            Bundle search = search(type, ifNoneExistsQuery);

            if (search.getEntry().size() == 0) {
                id = create(resource);
                status = 201;
            } else if (search.getEntry().size() == 1) {
                Resource match = search.getEntryFirstRep().getResource();
                id = match.getIdElement().getIdPart();
                status = 200;
            } else {
                return new ConditionalCreateResponse("Multiple matches found for conditional create", 412);
            }
            return new ConditionalCreateResponse(id, status);
        } catch (Exception ex) {
            return new ConditionalCreateResponse(ex.getMessage(), 500);
        }
    }

    /**
     * Executes a FHIR server operation defined for the given path and name.
     *
     * Note that the output of this method is dependant on the specific implementation being
     * invoked. Please consult the documentation for the specific {@link IOperationExecutor}
     * for the specifics on what type of output is returned.
     *
     * @param path Server path of the operation (e.g. Patient/[id])
     * @param operation Operation name without $ sign (e.g. everything)
     * @param parameters Any additional input needed for the operation
     * @param <T> Operation output
     * @return The result of the operation
     * @throws FhirProofException Indicating why the operation failed.
     */
    public <T> T executeOperation(String path, String operation, String parameters)
            throws FhirProofException {
        try {
            if (!executors.containsKey(operation)) {
                throw new FhirProofException(String.format("No operation defined for '%s'", operation));
            }

            IOperationExecutor<T> executor = executors.get(operation);
            return executor.execute(path, operation, parameters, store);
        } catch (Exception ex) {
            throw new FhirProofException(ex.getMessage());
        }
    }

    /**
     * Executes a FHIR transaction Bundle against the FHIR store.
     * @param transaction Input transaction Bundle.
     * @return A Bundle with the specific results of the transaction
     * @throws FhirProofException Indicating why the transaction failed.
     */
    public Bundle executeTransaction(Bundle transaction) throws FhirProofException {

        if (!transaction.hasType() || transaction.getType() != Bundle.BundleType.TRANSACTION) {
            throw new FhirProofException("Status 400: Bundle was not a Transaction");
        }

        Bundle response = new Bundle();
        response.setType(Bundle.BundleType.TRANSACTIONRESPONSE);

        for (Bundle.BundleEntryComponent entry : transaction.getEntry()) {
            Bundle.BundleEntryRequestComponent request = entry.getRequest();
            if (!request.hasMethod()) {
                throw new FhirProofException("Status 400: Bundle Entry did not contain a request method");
            }

            Bundle.BundleEntryComponent responseEntry = new Bundle.BundleEntryComponent();
            Bundle.BundleEntryResponseComponent responseComponent = new Bundle.BundleEntryResponseComponent();

            if (request.getMethod() == Bundle.HTTPVerb.DELETE) {
                String[] parts = request.getUrl().split("/");
                Resource current = read(parts[0], parts[1]);
                String version = current.getMeta().getVersionId();
                Date lastUpdate = current.getMeta().getLastUpdated();
                delete(parts[0], parts[1]);

                responseComponent.setEtag(String.format("W/\"%s\"", version));
                responseComponent.setLocation(FHIR_STORE_URL + request.getUrl());
                responseComponent.setLastModified(lastUpdate);
                responseComponent.setStatus("204 No Content");

            } else if (request.getMethod() == Bundle.HTTPVerb.GET) {
                String[] parts = request.getUrl().split("/");
                Resource current = read(parts[0], parts[1]);
                String version = current.getMeta().getVersionId();
                Date lastUpdate = current.getMeta().getLastUpdated();

                responseComponent.setEtag(String.format("W/\"%s\"", version));
                responseComponent.setLocation(FHIR_STORE_URL + request.getUrl());
                responseComponent.setLastModified(lastUpdate);
                responseComponent.setStatus("200 OK");
                responseEntry.setResource(current);

            } else if (request.getMethod() == Bundle.HTTPVerb.PUT) {

                Resource instance = entry.getResource();
                String[] parts = request.getUrl().split("/");
                String type = parts[0];
                String id = parts[1];

                update(type, id, instance);
                Resource current = read(type, id);
                String version = current.getMeta().getVersionId();
                Date lastUpdate = current.getMeta().getLastUpdated();

                responseComponent.setStatus("200 OK");
                responseComponent.setEtag(String.format("W/\"%s\"", version));
                responseComponent.setLocation(String.format("%s%s/%s", FHIR_STORE_URL, type, id));
                responseComponent.setLastModified(lastUpdate);

            } else if (request.getMethod() == Bundle.HTTPVerb.POST) {
                if (!entry.hasResource()) {
                    throw new FhirProofException("Status 400: PUT Transaction did not contain a resource");
                }
                Resource resource = entry.getResource();

                String id;
                String status;
                if (request.hasIfNoneExist()) {
                    String params = request.getIfNoneExist();
                    ConditionalCreateResponse storeResponse = conditionalCreate(resource, params);
                    id = storeResponse.getResponse();
                    if (storeResponse.getStatus() == 200) {
                        status = "200 OK";
                    } else if (storeResponse.getStatus() == 201) {
                        status = "201 Created";
                    } else {
                        throw new FhirProofException(storeResponse.getResponse());
                    }

                } else {
                    id = create(resource);
                    status = "201 Created";
                }
                Resource current = read(resource.getResourceType().name(), id);
                String version = current.getMeta().getVersionId();
                Date lastUpdate = current.getMeta().getLastUpdated();

                responseComponent.setStatus(status);
                responseComponent.setEtag(String.format("W/\"%s\"", version));
                responseComponent.setLocation(String.format("%s%s/%s", FHIR_STORE_URL, resource.getResourceType().name(), id));
                responseComponent.setLastModified(lastUpdate);
            } else {
                continue;
            }
            responseEntry.setResponse(responseComponent);
            response.addEntry(responseEntry);
        }
        return response;
    }
}
