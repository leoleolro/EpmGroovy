/**
 * Rule Name : ProjRep - Calling Substitution Variable Rule
 * Purpose   : Trigger a job to set substitution variables in RPEPM via REST API
 * Author    : Sneha Singh
 * Date      : <Add Date>
 */

import groovy.json.JsonOutput
import com.oracle.hyperion.rest.HttpResponse

// ----------------------------
// Step 1: Create JSON payload
// ----------------------------
def payload = new JSONObject()
        .put("jobType", "Rules")
        .put("jobName", "ProjRep - Set Substitution variable")
        .toString()

println("Payload for job submission: ${payload}")

// ----------------------------
// Step 2: Call REST API to execute the job
// ----------------------------
HttpResponse<String> jsonResponse = operation.application
        .getConnection("LocalConnection")
        .post("HyperionPlanning/rest/v3/applications/RPEPM/jobs")
        .body(payload)
        .asString()

// ----------------------------
// Step 3: Logging response
// ----------------------------
println("HTTP Status: " + jsonResponse.status)
println("Response Body: " + jsonResponse.body)
