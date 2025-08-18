Purpose : Used For Monthly Claimed Cost calculation based on Claimed Quantity and Sell Rate

/* RTPS: {Var_Dim_Entity} {Var_Dim_Project}*/

/* Get the cube where the calculations will take place*/
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()

String payitemEscalationCatAccount = ["Pay Item Escalation Category"]

// Get the Period and Year user variable.
String strPeriod= operation.application.getUserVariable("UV_Period").value.name
String strYear= operation.application.getUserVariable("UV_Year").value.name

/*Capture the edited Pay Item Escalation Category*/
Set&lt;List&lt;String>> editedTotalLineItem = []
operation.grid.dataCellIterator({DataCell cell-> payitemEscalationCatAccount.contains(cell.getAccountName()) &amp;&amp; cell.edited}).each{DataCell cell -> 
	editedTotalLineItem &lt;&lt; ([cell.getMemberName("Line Item"),cell.getMemberName("Account"),cell.getFormattedValue()]).toUnique()
    println ("${cell.getMemberNames()} : ${cell.crossDimCell("Bill Quantity").data} : ${cell.getFormattedValue()} : ${cell.getMemberName("Line Item")} : ${cell.getMemberName("Account")}")
    }


/* Write an entry into the log file if no cells have been edited and exit the script*/
if(editedTotalLineItem.size()==0){
 println("No cells were edited - V2")
}
else{
    /* Loop through each tuples to get line item and Rate combination. */
  	for (tuple in editedTotalLineItem) {
    	String payitems = tuple[0]
    	String escalation_accounts = tuple[1]
        String escalation_cat = tuple[2]

String povData = getCrossJoins([['[Plan]'],['[Working]'],['[Load]'],['[Month]'],[payitems],[mdxParams(rtps.Var_Dim_Entity.member)], [mdxParams(rtps.Var_Dim_Project.member)],[strPeriod],[strYear]])
		//calcParameters.pov= getCrossJoins(povData)
        calcParameters.pov=povData
        
        //println (calcParameters.pov)
  	

    //Creating sourceRegion
    String crossData = getCrossJoins([['[Bill Quantity]',escalation_cat,'[Pay Item Sell Rate]'],['[BegBalance]'],['[No Year]'], ['[No View]'], ['[Direct Input]'],['[No Line Item]']])
    //calcParameters.sourceRegion= getCrossJoins(crossData)
    calcParameters.sourceRegion=crossData
    //println(calcParameters.sourceRegion)


    String script1 = """
                        ([Claimed Cost]) := ([Bill Quantity]) * (([Pay Item Sell Rate],[No Year],[BegBalance],[No View]) * (1 + ([${escalation_cat}],[Direct Input],[No Line Item]))) ;

                  """

      /* Run the calculation script */
      calcParameters.script = script1
      calcParameters.roundDigits = 4
      cube.executeAsoCustomCalculation(calcParameters)

    }
}


/*Custom function to used to create a cross-join. The function expects a list to be provided*/
def getCrossJoins(List&lt;List&lt;String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members -> "CrossJoin(" + concat + ',{' + members.join(',') + '})' }
    }
    return crossJoinString
}</script></rule><rule id="2" name="ProjRep - Calculate Monthly Claimed Revenue" product="Planning"><property name="application">RPEPM</property><property name="plantype">ProjRep</property><variable_references><variable_reference name="Var_Dim_Entity" id="1"><property name="hidden">false</property><property name="rule_name">ProjRep - Calculate Monthly Claimed Revenue</property><property name="seq">1</property><property name="type">3</property><property name="useAsOverrideValue">false</property></variable_reference><variable_reference name="Var_Dim_Project" id="2"><property name="hidden">false</property><property name="rule_name">ProjRep - Calculate Monthly Claimed Revenue</property><property name="seq">2</property><property name="type">3</property><property name="useAsOverrideValue">false</property></variable_reference></variable_references><script type="groovy">/*Rule Name : rojRep - Calculate Monthly Claimed Revenue
Purpose : Used For Monthly Claimed Cost calculation based on Claimed Quantity and Sell Rate
Date Created: 30/01/2025
Created By: Sneha Singh
Changes Made: 
Change Date: 
*/


/* RTPS: {Var_Dim_Entity} {Var_Dim_Project} */

/* Get the cube where the calculations will take place*/
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()
String script1
/*def crossData*/

def payitemEscalationCatAccount = ["Bill Quantity"]

// Get the Period and Year user variable.
String strPeriod= operation.application.getUserVariable("UV_Period").value.name
String strYear= operation.application.getUserVariable("UV_Year").value.name

/*Capture the edited Bill Quantity */
Set&lt;List&lt;String>> editedTotalLineItem = []
operation.grid.dataCellIterator({DataCell cell-> payitemEscalationCatAccount.contains(cell.getAccountName()) &amp;&amp; cell.edited}).each{DataCell cell -> 
    editedTotalLineItem &lt;&lt; ([cell.getMemberName("Line Item"),cell.getMemberName("Account"), cell.crossDimCell("Pay Item Escalation Category").formattedValue, (cell.crossDimCell("Pay Item Escalation Category").data).toString()]).toUnique()
    
    println ("${cell.getMemberNames()} : ${cell.crossDimCell("Bill Quantity").data} : ${cell.crossDimCell("Pay Item Escalation Category")} : ${cell.crossDimCell("Pay Item Escalation Category").formattedValue} : ${cell.crossDimCell("Pay Item Escalation Category").data}")
    }


/* Write an entry into the log file if no cells have been edited and exit the script*/
if(editedTotalLineItem.size()==0){

 println("No cells were edited - V3")

}
else{
    /* Loop through each tuples to get line item and Rate combination. */
  	for (tuple in editedTotalLineItem) {
    	String payitems = tuple[0]
        String bill_quanity = tuple[1]
        String escalation_cat = tuple[2]
		String is_escalation_cat_blank = tuple[3]
     
  		String povData = getCrossJoins([['[Plan]'],['[Working]'],['[Load]'],['[Month]'],[payitems],[mdxParams(rtps.Var_Dim_Entity.member)], [mdxParams(rtps.Var_Dim_Project.member)],[strPeriod],[strYear]])
		calcParameters.pov=povData
        //calcParameters.pov=getCrossJoins(povData)
        println (calcParameters.pov)
	
    	if (is_escalation_cat_blank == "0.0")
    	{
    		String crossData = getCrossJoins([['[Bill Quantity]','[Pay Item Sell Rate]'],['[BegBalance]'],['[No Year]'], ['[No View]'], ['[Direct Input]'],['[No Line Item]']])
    		calcParameters.sourceRegion=crossData
            //calcParameters.sourceRegion= getCrossJoins(crossData)
            script1 = """([Claimed Cost]) := ([Bill Quantity]) * ([Pay Item Sell Rate],[No Year],[BegBalance],[No View]) ;"""
		 } 
     	else
     	{
        	String crossData = getCrossJoins([['[Bill Quantity]',escalation_cat,'[Pay Item Sell Rate]'],['[BegBalance]'],['[No Year]'], ['[No View]'], ['[Direct Input]'],['[No Line Item]']])
            calcParameters.sourceRegion=crossData
            //calcParameters.sourceRegion= getCrossJoins(crossData)
            script1 = """([Claimed Cost]) := ([Bill Quantity]) * (([Pay Item Sell Rate],[No Year],[BegBalance],[No View]) * (1 + ([${escalation_cat}],[Direct Input],[No Line Item]))) ;"""
    	}    	      
     
        calcParameters.script = script1
        calcParameters.roundDigits = 4
        cube.executeAsoCustomCalculation(calcParameters)

    }
}


/*Custom function to used to create a cross-join. The function expects a list to be provided*/
def getCrossJoins(List&lt;List&lt;String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members -> "CrossJoin(" + concat + ',{' + members.join(',') + '})' }
    }
    return crossJoinString
}</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - APPLY ESCALATION CATEGORY TO MONTHLY CLAIMED COST"/><deployobject product="2" application="rpepm" plantype="projrep" obj_id="2" obj_type="1" name="PROJREP - CALCULATE MONTHLY CLAIMED REVENUE"/><deployobject product="2" application="rpepm" obj_id="1" obj_type="2" name="PROJREP - CLAIMED COST CALCULATION RULESET"/></deployobjects></HBRRepo>