
<HBRRepo>
  <variables>
    <!-- Variables for Entity, Project, Pay Item Name and Number -->
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
    <variable name="var_PayItemName" type="str" usage="const" id="4" product="Planning">
      <property name="prompt_text">Pay Item Name</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>
    <variable name="var_PayItemNumber" type="str" usage="const" id="3" product="Planning">
      <property name="prompt_text">Pay Item Number</property>
      <property name="scope">ruleset</property>
      <value/>
    </variable>
  </variables>
  <rulesets/>
  <rules>
    <rule id="1" name="ProjRep - Add Variation Pay Item" product="Planning">
      <property name="application">RPEPM</property>
      <property name="plantype">ProjRep</property>
      <variable_references>
        <!-- References to input variables -->
        <variable_reference name="Var_Dim_Entity" id="1"><property name="hidden">true</property></variable_reference>
        <variable_reference name="Var_Dim_Project" id="2"><property name="hidden">true</property></variable_reference>
        <variable_reference name="var_PayItemName" id="4"><property name="hidden">false</property></variable_reference>
        <variable_reference name="var_PayItemNumber" id="3"><property name="hidden">false</property></variable_reference>
      </variable_references>
      <script type="groovy">
/* 
  Rule Name : ProjRep - Add Variation Pay Item
  Purpose   : Create Pay Item Variations dynamically in the Line Item dimension of ProjRep cube.
              Ensures parent members exist, validates uniqueness, and adds line data to the cube.
  Date Created: 05/07/2025
  Created By: SN
*/

// === Setup message bundles for validation messages ===
def mbUs = messageBundle([
    "validation.invalidPayItemNumber":"The Pay Item Number is invalid please ensure that it is alphanumeric: {0}",
    "validation.memberexists":"The member already exists so please create a member with a different number: {0}"
])
def mbl = messageBundleLoader(["en" : mbUs]);

// === Validate the Pay Item Number input ===
validateRtp(rtps.var_PayItemNumber, /^[a-zA-Z0-9_-]{1,50}$/, mbl, "validation.invalidPayItemNumber", rtps.var_PayItemNumber);

// === Extract and format project and Pay Item identifiers ===
String strFullProjectNumber = stripQuotes(rtps.Var_Dim_Project.toString())
String strProjectNumber = strFullProjectNumber.tokenize('_')[1]                  // second token of project number
String strPayItemNumber = strProjectNumber + "_" + stripQuotes(rtps.var_PayItemNumber.toString())  
String strPayItemName = strProjectNumber + "-" + stripQuotes(rtps.var_PayItemNumber.toString()) + "-" + stripQuotes(rtps.var_PayItemName.toString())
String strEntityName = stripQuotes(rtps.Var_Dim_Entity.toString())

// Define parent members
String StrPayItemParent = strProjectNumber + "_PI"
String StrPayItemVariationsParent = strProjectNumber + "_Variations"
String StrPayItemVariationsParentAlias = strProjectNumber + "-Variations"

// === Access cube and Line Item dimension ===
Cube cube = operation.application.getCube("ProjRep")
Dimension LineItemDim = operation.application.getDimension("Line Item")
Member ProjectPayItemParent = LineItemDim.getMember(StrPayItemParent)
Member TotalProjectPayItems = LineItemDim.getMember("Project Pay Items")

// === Get children of the project parent ===
def mbrProjectPayItemParentChildren = LineItemDim.getEvaluatedMembers(/CHILDREN("${ProjectPayItemParent}")/, cube)*.name

if(mbrProjectPayItemParentChildren.contains(StrPayItemVariationsParent)){
    // Variation parent exists, check if the specific variation already exists
    Map newPayItemVariationsParent = ProjectPayItemParent.newChildAsMap(StrPayItemVariationsParent)
    newPayItemVariationsParent["Alias: Default"] = StrPayItemVariationsParentAlias
    Member PayItemVariationParent = LineItemDim.saveMember(newPayItemVariationsParent, DynamicChildStrategy.ALWAYS_DYNAMIC)

    println "Variation parent exists. Proceeding to check if variation ${strPayItemNumber} exists"
    Member ProjectPayItemVariationsParent = LineItemDim.getMember(StrPayItemVariationsParent)

    if(ProjectPayItemVariationsParent.hasChild(strPayItemNumber)){
        throwVetoException(mbl, "validation.memberexists", strPayItemNumber)
    } else {
        println "Creating variation ${strPayItemNumber}"
        Map newPayItemVariationLine = ProjectPayItemVariationsParent.newChildAsMap(strPayItemNumber)
        newPayItemVariationLine["Alias: Default"] = strPayItemName
        Member PayItemVariationLine = LineItemDim.saveMember(newPayItemVariationLine, DynamicChildStrategy.ALWAYS_DYNAMIC)
        println "Variation ${strPayItemNumber} created"
    }
} else {
    // Variation parent does not exist, create it
    println "Variation parent ${StrPayItemVariationsParent} does not exist. Creating it"
    Map newProjectPayItemParent = TotalProjectPayItems.newChildAsMap(ProjectPayItemParent)
    Member UpdatePayItemVariationParent = LineItemDim.saveMember(newProjectPayItemParent, DynamicChildStrategy.ALWAYS_DYNAMIC)

    Map newPayItemVariationsParent = ProjectPayItemParent.newChildAsMap(StrPayItemVariationsParent)
    newPayItemVariationsParent["Alias: Default"] = StrPayItemVariationsParentAlias
    Member PayItemVariationParent = LineItemDim.saveMember(newPayItemVariationsParent, DynamicChildStrategy.ALWAYS_DYNAMIC)

    Member ProjectPayItemVariationsParent = LineItemDim.getMember(StrPayItemVariationsParent)

    println "Creating variation ${strPayItemNumber}"
    Map newPayItemVariationLine = ProjectPayItemVariationsParent.newChildAsMap(strPayItemNumber)
    newPayItemVariationLine["Alias: Default"] = strPayItemName
    Member PayItemVariationLine = LineItemDim.saveMember(newPayItemVariationLine, DynamicChildStrategy.ALWAYS_DYNAMIC)
    println "Variation ${strPayItemNumber} created"
}

// === Prepare a DataGrid to store the variation in the cube ===
DataGridBuilder builder = cube.dataGridBuilder("MM/DD/YYYY")
builder.addPov('No Year', 'Working', 'Plan', strEntityName, strFullProjectNumber, 'Load', 'No View', 'BegBalance')
builder.addColumn('Pay Item Name')
builder.addRow([strPayItemNumber], [strPayItemName])

DataGridBuilder.Status status = new DataGridBuilder.Status()
builder.build(status).withCloseable { grid ->
    println("Total accepted cells: $status.numAcceptedCells")
    println("Total rejected cells: $status.numRejectedCells")
    println("First 100 rejected cells: $status.cellsRejected")
    try {
        cube.saveGrid(grid)
    } catch (e) {
        println e.message
    }
}

      </script>
    </rule>
  </rules>
  <components/>
  <deployobjects>
    <deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - ADD VARIATION PAY ITEM"/>
  </deployobjects>
</HBRRepo>
