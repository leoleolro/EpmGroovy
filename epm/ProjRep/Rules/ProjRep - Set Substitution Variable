Purpose : This rule is to set the substitution variable for Plan Month, Plan Year,Act Month, Act Year.

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


</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - SET SUBSTITUTION VARIABLE"/></deployobjects></HBRRepo>