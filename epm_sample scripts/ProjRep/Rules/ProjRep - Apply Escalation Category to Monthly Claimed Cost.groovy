
<HBRRepo>
	<variables>
		<variable name="Var_Dim_Entity" type="member" usage="const" id="1" product="Planning">
			<property name="dimensionInputMode">type</property>
			<property name="dimensionType">Entity</property>
			<property name="prompt_text">Please Select The Entity</property>
			<property name="scope">ruleset</property>
			<value/>
		</variable>
		<variable name="Var_Dim_Project" type="member" usage="const" id="2" product="Planning">
			<property name="dimension">Project</property>
			<property name="dimensionInputMode">name</property>
			<property name="prompt_text">Please Select the Project</property>
			<property name="scope">ruleset</property>
			<value/>
		</variable>
	</variables>
	<rulesets/>
	<rules>
		<rule id="1" name="ProjRep - Apply Escalation Category to Monthly Claimed Cost" product="Planning">
			<property name="application">RPEPM</property>
			<property name="plantype">ProjRep</property>
			<variable_references>
				<variable_reference name="Var_Dim_Entity" id="1">
					<property name="hidden">true</property>
					<property name="rule_name">ProjRep - Apply Escalation Category to Monthly Claimed Cost</property>
					<property name="seq">1</property>
					<property name="type">3</property>
					<property name="useAsOverrideValue">false</property>
				</variable_reference>
				<variable_reference name="Var_Dim_Project" id="2">
					<property name="hidden">true</property>
					<property name="rule_name">ProjRep - Apply Escalation Category to Monthly Claimed Cost</property>
					<property name="seq">2</property>
					<property name="type">3</property>
					<property name="useAsOverrideValue">false</property>
				</variable_reference>
			</variable_references>
			<script type="groovy">
/**
 * Rule: ProjRep - Apply Escalation Category to Monthly Claimed Cost
 * Purpose: This rule calculates the 'Monthly Claimed Cost' for any edited pay items.
 * The calculation is based on the 'Bill Quantity' multiplied by a 'Pay Item Sell Rate'
 * which is adjusted by an escalation factor derived from the selected 'Pay Item Escalation Category'.
 * This script runs on an ASO cube and is triggered when a user saves data in a form.
 */

// Define the runtime prompts (RTPs) that will be passed to the script.
// These are hidden from the user and capture the Entity and Project from the form's context.
/* RTPS: {Var_Dim_Entity} {Var_Dim_Project}*/

// Establish a connection to the ProjRep (ASO) cube.
Cube cube = operation.getApplication().getCube('ProjRep')

// Initialize the parameters object that will be used to execute the ASO custom calculation.
CustomCalcParameters calcParameters = new CustomCalcParameters()

// Define the specific account that triggers the calculation when its data is edited.
def triggerAccount = ["Pay Item Escalation Category"]

// Retrieve the current Period and Year from the user variables to set the calculation context.
String strPeriod = operation.application.getUserVariable("UV_Period").value.name
String strYear = operation.application.getUserVariable("UV_Year").value.name

// --- Step 1: Identify Edited Cells ---
// Iterate through all cells in the form's grid to find which ones were edited.
// We are only interested in cells belonging to the 'Pay Item Escalation Category' account.
Set&lt;List&lt;String&gt;&gt; editedPayItems = []
operation.grid.dataCellIterator({ DataCell cell -> 
    // Check if the cell's account is the one we're looking for AND if the cell has been edited.
    triggerAccount.contains(cell.getAccountName()) &amp;&amp; cell.isEdited() 
}).each { DataCell cell -> 
    // For each edited cell, capture a list containing the Line Item, Account, and the new value (the escalation category).
    // Add this list to a Set, which automatically handles duplicates.
    editedPayItems &lt;&lt; [cell.getMemberName("Line Item"), cell.getMemberName("Account"), cell.getFormattedValue()]
}

// --- Step 2: Execute Calculation or Exit ---
// Check if any relevant cells were actually edited.
if (editedPayItems.isEmpty()) {
    // If no cells were edited, print a message to the job log and stop the script.
    println("No 'Pay Item Escalation Category' cells were edited. Calculation skipped.")
} else {
    // If cells were edited, loop through each unique combination of edited pay items.
    editedPayItems.each { List&lt;String&gt; tuple ->
        // Extract the Line Item, Account, and Escalation Category from the tuple.
        String payItem = tuple[0]
        String escalationAccount = tuple[1] // Not used in calculation script, but captured for context.
        String escalationCategory = tuple[2]

        // --- Step 3: Construct the MDX Calculation ---
        // Define the Point of View (POV) for the calculation. This specifies the exact intersection
        // where the calculation will be performed, using a combination of fixed members and dynamic values
        // from the form context (RTPs) and user variables.
        String pov = getCrossJoins([
            ['Plan'], ['Working'], ['Load'], ['Month'], // Static dimension members
            [payItem],                                  // The specific Line Item that was edited
            [rtps.Var_Dim_Entity.name],                 // Entity from the form's context
            [rtps.Var_Dim_Project.name],                // Project from the form's context
            [strPeriod],                                // Period from user variables
            [strYear]                                   // Year from user variables
        ])
        calcParameters.pov = pov
        
        // Define the MDX formula script to perform the calculation.
        // This is a GString, which allows embedding the 'escalationCategory' variable directly.
        String calculationScript = """
            /* Calculate Claimed Cost based on Bill Quantity and escalated Sell Rate */
            ([Claimed Cost]) := 
                ([Bill Quantity]) * (
                    ([Pay Item Sell Rate], [No Year], [BegBalance], [No View]) * (1 + ([${escalationCategory}], [Direct Input], [No Line Item]))
                );
        """
        
        // --- Step 4: Run the ASO Custom Calculation ---
        // Assign the MDX script to the parameters object.
        calcParameters.script = calculationScript
        calcParameters.roundDigits = 4 // Optionally, set rounding precision.

        // Execute the custom calculation on the ASO cube with the defined POV and script.
        cube.executeAsoCustomCalculation(calcParameters)
        println("Calculated Claimed Cost for Pay Item: ${payItem} using Escalation Category: ${escalationCategory}.")
    }
}


/**
 * A helper function to dynamically create a nested MDX CrossJoin string.
 * This is used to build the POV for the ASO calculation.
 * @param essIntersection A List of Lists, where each inner list contains member names for one dimension.
 * @return A formatted MDX string, e.g., "CrossJoin({FY25}, CrossJoin({Jan}, {Actual}))".
 */
String getCrossJoins(List&lt;List&lt;String&gt;&gt; essIntersection) {
    // Return an empty string if the input list is null or empty to prevent errors.
    if (!essIntersection) {
        return ''
    }
    
    // If there's only one dimension, format it as a set, e.g., "{member1, member2}".
    if (essIntersection.size() == 1) {
        return "{${essIntersection[0].join(',')}}"
    }

    // For multiple dimensions, build a nested CrossJoin string.
    // Start with the first dimension's members formatted as a set.
    String initialSet = "{${essIntersection[0].join(',')}}"
    
    // Use inject (a fold operation) to iteratively wrap each subsequent dimension's set in a CrossJoin function.
    return essIntersection[1..-1].inject(initialSet) { String cumulative, List&lt;String&gt; members ->
        "CrossJoin(${cumulative}, {${members.join(',')}})"
    }
}
			</script>
		</rule>
	</rules>
	<components/>
	<deployobjects>
		<deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - APPLY ESCALATION CATEGORY TO MONTHLY CLAIMED COST"/>
	</deployobjects>
</HBRRepo>