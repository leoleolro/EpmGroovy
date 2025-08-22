Purpose : This rule is to clear the rates data from form 1.2 PayItem by cost code.


/* RTPS: {Var_Dim_Entity} {Var_Dim_Project}  */
operation.getApplication().getCube('ProjRep').clearPartialData("CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin(CrossJoin({[Plan]},{[Working]}),{[No Year]}),{[Load]}),{[No View]}),{[${rtps.Var_Dim_Entity.member}]}),{[${rtps.Var_Dim_Project.member}]}),{[BegBalance]}),{Descendants([Total Line Item], [Total Line Item].dimension.Levels(0))}),{Descendants([Project Cost Code Rates], [Project Cost Code Rates].dimension.Levels(0))})",true)
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROREP - CLEAR RATES DATA ACTION MENU"/></deployobjects></HBRRepo>