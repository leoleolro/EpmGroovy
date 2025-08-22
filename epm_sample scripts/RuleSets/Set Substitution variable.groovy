
def payload = new JSONObject()
  .put("jobType","Rules")
  .put("jobName","ProjRep - Set Substitution variable")
  .toString()

HttpResponse&lt;String> jsonResponse
jsonResponse = operation.application.getConnection("LocalConnection").post("HyperionPlanning/rest/v3/applications/RPEPM/jobs").body(payload).asString()
println(jsonResponse.status)
println(jsonResponse.body)
    </script></rule><rule id="2" name="ProjRep - Set Substitution Variable" product="Planning"><property name="application">RPEPM</property><property name="plantype">ProjRep</property><variable_references><variable_reference name="Var_Dim_Entity" id="1"><property name="hidden">false</property><property name="rule_name">ProjRep - Set Substitution Variable</property><property name="seq">1</property><property name="type">3</property><property name="useAsOverrideValue">false</property></variable_reference><variable_reference name="Var_Dim_Project" id="2"><property name="hidden">false</property><property name="rule_name">ProjRep - Set Substitution Variable</property><property name="seq">2</property><property name="type">3</property><property name="useAsOverrideValue">false</property></variable_reference></variable_references><script type="groovy">/*Rule Name : ProjRep - Set Substitution Variable
Purpose : This rule is to set the substitution variable for Plan Month, Plan Year,Act Month, Act Year.
Date Created: 13/02/2025
Created By: SS
Changes Made:
Change Date:
*/

/* RTPS: {Var_Dim_Entity} {Var_Dim_Project}  */


String strPlanYear
String strPlanMonth
String strActYear
String strActMonth
String tmpPlanYear
String tmpActYear


/*Capture the Smartlist */
List&lt;String> strSubstitutionVar = []
operation.grid.dataCellIterator().each {
	strSubstitutionVar.add(it.formattedValue)
    }

/* Assign to variable */
tmpPlanYear = strSubstitutionVar[0]
strPlanYear = "FY" + tmpPlanYear.substring(2,4)
strPlanMonth = strSubstitutionVar[1]
tmpActYear = strSubstitutionVar[2]
strActYear = "FY" + tmpActYear.substring(2,4)
strActMonth = strSubstitutionVar[3]

/* Set the Substitution Variable */
operation.application.setSubstitutionVariableValue("PlanYear", strPlanYear)
operation.application.setSubstitutionVariableValue("PlanMonth", strPlanMonth)
operation.application.setSubstitutionVariableValue("ActYear", strActYear)
operation.application.setSubstitutionVariableValue("ActMonth", strActMonth)


</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - CALLING SUBSTITUTION VARIABLE RULE"/><deployobject product="2" application="rpepm" plantype="projrep" obj_id="2" obj_type="1" name="PROJREP - SET SUBSTITUTION VARIABLE"/><deployobject product="2" application="rpepm" obj_id="1" obj_type="2" name="SET SUBSTITUTION VARIABLE"/></deployobjects></HBRRepo>