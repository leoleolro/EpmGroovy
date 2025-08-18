Purpose : This rule is to check if there is a zero or blank entry for escalation category

/* RTPS:  */

def payitemEscalationCatAccount = ["Pay Item Escalation Category"]
def mbUs = messageBundle( ["validation.BlankCategory":"Escalation Category column cannot be blank."])
def mbl = messageBundleLoader(["en" : mbUs])


/*Capture the edited Pay Item Escalation Category*/
Set&lt;List&lt;String>> editedProjectPayItems = []
operation.grid.dataCellIterator({DataCell cell-> payitemEscalationCatAccount.contains(cell.getAccountName()) &amp;&amp; cell.edited}).each{DataCell cell -> 
	
    if (cell.getFormattedValue() == "0.0") {
        	throwVetoException(mbl, "validation.BlankCategory")
        }   
    }</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - MONTHLY CLAIMED COST FORM CHECK BLANK ESCALATION CATEGORY"/></deployobjects></HBRRepo>