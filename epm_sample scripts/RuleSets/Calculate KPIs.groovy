Purpose : Move the measures loaded into the Physicals to the Stats Cube 
Date Created: 11/06/2025

/* RTPS: */

/**************************************************************************************/
/* Use groovy to copy dynamic members to stored members               
/**************************************************************************************/
 
 Application app = operation.application
 Cube cube = app.getCube("Physical")
 
/*  Get list of Periods from Jan - Actual Month  */
List periodRange = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
def actPd = operation.application.getSubstitutionVariableValue("&amp;ActMonth")
def pdNum = periodRange.findIndexOf{ name -> name =~ actPd}
def actPds = periodRange[0..pdNum]
String actPdsStr = actPds.join(", ")

println "actPdsStr: $actPdsStr \n"

/*def ProjectsDim=app.getDimension("Project", cube)
def usrProjectMbrs = ProjectsDim.getEvaluatedMembers("""IDESCENDANTS(${rtps.Var_Dim_Project.toString()})""" as String, cube)

println "usrProjectMbrs: $usrProjectMbrs \n"*/
 
/**************************************************************************************/
/* Step 1: This section creates a grid with the dynamic members 
/**************************************************************************************/
 
DataGridDefinitionBuilder sourceBuilder = cube.dataGridDefinitionBuilder()
 
sourceBuilder.setSuppressMissingRowsNative(true)
sourceBuilder.setSuppressMissingBlocks(true)
sourceBuilder.setSuppressMissingSuppressesZero(true)
 
sourceBuilder.addPov([ 'Version', 'Scenario', 'View'],
                    [  ['Final'], ['Actual'], ['Month']  ] )
 
sourceBuilder.addColumn(['Year'], [ ["&amp;ActYear"] ])
 
 
sourceBuilder.addRow([ 'Period' ,'Account', 'Entity', 'Asset', 'Plan Element', 'Day', 'Project'],
                       [
                       ['ILvl0Descendants("YearTotal")'],
                       ['ILvl0Descendants("Physicals Measures")'],
                       ['ILvl0Descendants("All Entities")'],
                       ['ILvl0Descendants("Mining_Equipment")'],
                       ['ILvl0Descendants("Total Plan")'],
                       ['Total Days'],
                       ['ILvl0Descendants("Operational Projects")']
                     ])
                      
DataGridDefinition sourceGridDef = sourceBuilder.build()
def targetRows = []
 
/**************************************************************************************/
/* Step 2: This step applies the mapping of dynamic member to store member            */
/**************************************************************************************/
cube.loadGrid(sourceGridDef, false).withCloseable { sourceGrid ->
 
  Map mappings = [
                /* Copy dynamic member to store member for auto predict */
                    'Total Days' : 'ALT_Total Days',  
                  ]
                 
 
  sourceGrid.rows.each { row ->
    List&lt;String> rowHeaders = row.headers.collect{mappings.containsKey(it.mbrName) ? mappings[it.mbrName] : it.mbrName}
    def rowData = []
    rowData = row.data.collect{it.missing ? '#Missing' : it.data}
    targetRows &lt;&lt; [rowHeaders, rowData]
}
}
 
/******************************************************************************************/
/* Step 3: This step creates the grid where the data will be copied to and copies the data                                 
/******************************************************************************************/
 
/* If the source grid (targetRows) contains data, continue, else go to the end of the rule without error */
if(targetRows){
 
DataGridBuilder targetGrid = cube.dataGridBuilder("MM/DD/YYYY") 
targetGrid.addPov('Final', 'Actual', 'Month')
targetGrid.addColumn ("&amp;ActYear")
 
targetRows.each {  row ->
 
   List listRow = (List) row
   targetGrid.addRow((List&lt; String>) listRow[0] , (List&lt;Double>) listRow[1])
 
}
 
DataGridBuilder.Status status = new DataGridBuilder.Status()
 
 
targetGrid.build(status).withCloseable { grid ->
  println("Total number of cells accepted: $status.numAcceptedCells")
  println("Total number of cells rejected: $status.numRejectedCells")
  println("First 100 rejected cells: $status.cellsRejected")
 
  // Save the data to the cube
 
  cube.saveGrid(grid)
}
}

/******************************************************************************************/
/* Step 4: This step pushes the data to the STATS Cube                                  
/******************************************************************************************/

if(operation.application.hasDataMap("Physicals To Stats"))
	operation.application.getDataMap("Physicals To Stats").execute(true)

</script></rule><rule id="2" name="Stats - Calculate KPIs - Night" product="Planning"><property name="application">RPEPM</property><property name="plantype">Stats</property><statement seq="1" type="component" kind="script" name=""><component type="script" id="-100" name="" product="Planning"><property name="application">RPEPM</property><property name="plantype">Stats</property><script type="calcscript">SET EMPTYMEMBERSETS ON;
SET UPDATECALC ON;
SET FRMLRTDYNAMIC OFF;
SET COPYMISSINGBLOCK ON;</script></component></statement><statement seq="2" type="loop"><loop product="Planning" type="data"><property name="application">RPEPM</property><property name="plantype">Stats</property><property name="display_height">2</property><property name="display_width">3</property><property name="loop_type">meta</property><property name="Tile_Key">x2y0</property><test><expression><source><slice><dimension name="Entity" seq="1"><expression><user_inp>@Relative("All Entities", 0)</user_inp></expression></dimension><dimension name="Account" seq="2"><expression><user_inp>@Relative("Physicals Measures", 0), @Relative("Physicals Costs", 0)</user_inp></expression></dimension><dimension name="Period" seq="3"><expression><user_inp>@Relative("YearTotal", 0)</user_inp></expression></dimension><dimension name="Scenario" seq="4"><expression><user_inp>Actual</user_inp></expression></dimension><dimension name="Version" seq="5"><expression><user_inp>Final</user_inp></expression></dimension><dimension name="View" seq="6"><expression><user_inp>Month</user_inp></expression></dimension><dimension name="Year" seq="7"><expression><user_inp>&amp;ActYear</user_inp></expression></dimension><dimension name="Project" seq="8"><expression><user_inp>@Relative("All Projects", 0)</user_inp></expression></dimension></slice></source></expression></test><statement seq="1" type="component" kind="script" name=""><component type="script" id="-102" name="" product="Planning"><property name="application">RPEPM</property><property name="plantype">Stats</property><script type="calcscript">Fix (@IDescendants("Mining_Equipment")) 
    /*Calculating truck Specific KPIs*/
    "Calculated" = "Load" + "Direct Input";
Endfix 
</script></component></statement></loop></statement><statement seq="3" type="loop"><loop product="Planning" type="data"><property name="application">RPEPM</property><property name="plantype">Stats</property><property name="display_height">2</property><property name="display_width">3</property><property name="loop_type">meta</property><property name="Tile_Key">x5y0</property><test><expression><source><slice><dimension name="Entity" seq="1"><expression><user_inp>@Relative("All Entities", 0)</user_inp></expression></dimension><dimension name="Period" seq="2"><expression><user_inp>@Relative("YearTotal", 0)</user_inp></expression></dimension><dimension name="Scenario" seq="3"><expression><user_inp>Actual</user_inp></expression></dimension><dimension name="Version" seq="4"><expression><user_inp>Final</user_inp></expression></dimension><dimension name="View" seq="5"><expression><user_inp>Month</user_inp></expression></dimension><dimension name="Year" seq="6"><expression><user_inp>&amp;ActYear</user_inp></expression></dimension><dimension name="Plan Element" seq="7"><expression><user_inp>"Calculated"</user_inp></expression></dimension><dimension name="Project" seq="8"><expression><user_inp>@Relative("All Projects", 0)</user_inp></expression></dimension></slice></source></expression></test><statement seq="1" type="component" kind="script" name=""><component type="script" id="-104" name="" product="Planning"><property name="application">RPEPM</property><property name="plantype">Stats</property><script type="calcscript">Fix (@RELATIVE("Truck", 0))

    /*Calculating truck Specific KPIs*/
    "Costs/engine hour" = "Cost DB" / "ENGINE";
    "Costs/engine hour (All Maintenance)" = "Cost DB" / "ENGINE";
    "Costs/engine hour (GET)" = "TBD_GETCosts" / "ENGINE";
    "Costs/engine hour (Tyres)" = "TBD_TyreCosts" / "ENGINE";
    "Average Haul" = "TKM Meter" / "TUB_TONNES_NI";
    "tkms/hr" = "TKM_METER" / "ENGINE";
Endfix 
Fix (@RELATIVE("Underground Loader", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/engine hour" = "Cost DB" / "ENGINE";
    "Costs/engine hour (GET)" = "TBD_GETCosts" / "ENGINE";
    "Costs/engine hour (Tyres)" = "TBD_TyreCosts" / "ENGINE";
    /* This needs to be updated it it is measured*/
    /* Add if statement to check if the asset is in use, review stament from Rory*/
    "Tonnes per hour" = ("Total Loader Tonnes - Loaded to trucks" + "Total Loader Tonnes - CRF" + "Total Loader Tonnes - Waste Fill") / "ENGINE";
    "Perc Remoting" = "BUCKET_TONNES_REMOTE_NI" / ("BUCKET_TONNES_REMOTE_NI" + "BUCKET_TONNES_NI");
Endfix 
Fix (@Relative("Surface Wheel Loader", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/engine hour" = "Cost DB" / "ENGINE";
    "Costs/engine hour (GET)" = "TBD_GETCosts" / "ENGINE";
    "Costs/engine hour (Tyres)" = "TBD_TyreCosts" / "ENGINE";
    /* This needs to be updated it it is measured*/
    /* Add if statement to check if the asset is in use, review stament from Rory*/
    /* Are the stats for surface loaders the same as underground loaders*/
    "Tonnes per hour" = ("Total Loader Tonnes - Loaded to trucks" + "Total Loader Tonnes - CRF" + "Total Loader Tonnes - Waste Fill") / "ENGINE";
Endfix 
Fix (@RELATIVE("Production Drill", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/drill metre" = "Cost DB" / ("GS_DRILL_MTR" + "DEV_DRILL_MTR");
    "Costs/drill metre for drill consumables" = "Drill Cons Costs" / ("GS_DRILL_MTR" + "DEV_DRILL_MTR");
    "Costs/percussion hour" = "Cost DB" / "PERCUSSION_HR";
    "Drill Metres Per Percussion Hour" = "PROD_DRILL_MTR" / "PERCUSSION_HR";
    /*Are we using the meter for the Production drill meters? If so there is no split in hole cleaning etc*/
    /*This is a question for Michael Lenz*/
    "Drill metres per engine hour" = "PROD_DRILL_MTR" / "ENGINE";
    "Perc Uphole vs Downhole" = "PROD_DRILL_MTR" / "PROD_DRILL_MTR";
Endfix 
Fix (@RELATIVE("Development Drill", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/drill metre" = "Cost DB" / ("GS_DRILL_MTR" + "DEV_DRILL_MTR");
    "Costs/drill metre for drill consumables" = "Drill Cons Costs" / ("GS_DRILL_MTR" + "DEV_DRILL_MTR");
    "Costs/percussion hour" = "Cost DB" / "PERCUSSION_HR";
    "Drill Metres Per Percussion Hour" = ("GS_DRILL_MTR" + "DEV_DRILL_MTR") / "PERCUSSION_HR";
    "Drill metres per metre advanced (Face Drilling)" = "DEV_DRILL_MTR" / "Total Advanced Metres";
    "Drill metres per metre advanced (Ground Support)" = "GS_DRILL_MTR" / "Total Advanced Metres";
    "Total Drill metres per metre advanced" = ("GS_DRILL_MTR" + "DEV_DRILL_MTR") / "Total Advanced Metres";
    "Drill Metres Per Percussion Hour" = ("DEV_DRILL_MTR" + "GS_DRILL_MTR") / "PERCUSSION_HR";
    "Drill Metres Per Engine Hour" = ("DEV_DRILL_MTR" + "GS_DRILL_MTR") / "ENGINE";
    "Perc Face Drilling vs Total Drill metres" = "DEV_DRILL_MTR" / ("DEV_DRILL_MTR" + "GS_DRILL_MTR");
Endfix 
Fix (@RELATIVE("Ground Support Drill", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/installed metre" = "Cost DB" / "CABLE_BOLT_INSTALL_MTR";
    "Costs/percussion hour" = "Cost DB" / "PERCUSSION_HR";
    "Costs/drill metre for drill consumables" = "Drill Cons Costs" / "CABLE_BOLT_INSTALL_MTR";
    "Drill Metres Per Percussion Hour" = "CABLE_BOLT_INSTALL_MTR" / "PERCUSSION_HR";
    "Cabolt_Drill metres per engine hour" = "CABLE_BOLT_INSTALL_MTR" / "ENGINE";
    /* Confirm with Rory the calculation of this*/
    /* What is there a capture for the uphole / downhope or is this manually entered?*/
    "Perc Uphole vs Downhole" = "PROD_DRILL_MTR" / "PROD_DRILL_MTR";
Endfix 

Fix (@RELATIVE("Shotcrete Combo Truck", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/engine hour" = "Cost DB" / "ENGINE";
    "Costs/cubes applied" = "Cost DB" / "CEM_M3";
    "Cubes applied /engine hour" = "CEM_M3" / "ENGINE";
    "Hydroscaler Mins/ applied cubes" = ("HYDROSCALER" * 60) / "CEM_M3";
    /* Confirm how to handle the two Electric Meters*/
    "Electric Hours used / applied cubes" = "ELECTRIC_MOTOR_P1" / "CEM_M3";
    "Shotcrete Accelerator Cost / cubes applied" = "Accelerator Cost" / "CEM_M3";
    "Shotcrete Accelerator Litres / cubes applied" = "Accelerator Litres Used" / "CEM_M3";
    "Shotcrete Consumables costs / cubes applied" = "TBD_ShotCreteCons" / "CEM_M3";
Endfix

Fix (@RELATIVE("Underground Agitator Truck", 0)) 
    /*Calculating Cost KPIs*/
    "Costs/engine hour" = "Cost DB" / "ENGINE";
    "Costs/cubes Transported" = "Cost DB" / "CEM_M3";
    "Cubes Transported /engine hour" = "CEM_M3" / "ENGINE";
Endfix 

Fix (@RELATIVE("Batch Plant", 0),@RELATIVE("Paste Plant", 0)) 
    
    "Costs/m3 batched - maintenance" = "Cost DB" / "CEM_M3";
    "Costs/m3 - materials" = "Batching Costs" / "CEM_M3";
Endfix 
Fix(@RELATIVE("Crusher", 0)) 
    /*We need to work through Cubes through Plant*/
    "Costs/usage hour - maintenance" = "Cost DB" / "Usage Hours";
    "Costs/tonne crushed" = "Cost DB" / "CRUSHED_TONNES";
Endfix 
</script></component></statement></loop></statement><statement seq="4" type="loop"><loop product="Planning" type="data"><property name="application">RPEPM</property><property name="plantype">Stats</property><property name="display_height">2</property><property name="display_width">3</property><property name="loop_type">meta</property><property name="Tile_Key">x8y0</property><test><expression><source><slice><dimension name="Entity" seq="1"><expression><user_inp>@Relative("All Entities", 0)</user_inp></expression></dimension><dimension name="Account" seq="2"><expression><user_inp>@Relative("Physicals Measures", 0), @Relative("Physicals Costs", 0)</user_inp></expression></dimension><dimension name="Period" seq="3"><expression><user_inp>@Relative("YearTotal", 0)</user_inp></expression></dimension><dimension name="Scenario" seq="4"><expression><user_inp>Actual</user_inp></expression></dimension><dimension name="Version" seq="5"><expression><user_inp>Final</user_inp></expression></dimension><dimension name="View" seq="6"><expression><user_inp>Month</user_inp></expression></dimension><dimension name="Year" seq="7"><expression><user_inp>&amp;ActYear</user_inp></expression></dimension><dimension name="Project" seq="8"><expression><user_inp>@Relative("All Projects", 0)</user_inp></expression></dimension></slice></source></expression></test><statement seq="1" type="component" kind="script" name=""><component type="script" id="-106" name="" product="Planning"><property name="application">RPEPM</property><property name="plantype">Stats</property><script type="calcscript">Fix (@IDescendants("Mining_Equipment")) 
    /*Calculating truck Specific KPIs*/
    FIX ("Calculated") 
        CLEARBLOCK ALL;
    ENDFIX 
Endfix 
</script></component></statement></loop></statement></rule><rule id="3" name="Stats - Push KPIs to Physicals" product="Planning"><property name="application">RPEPM</property><property name="plantype">Stats</property><script type="groovy">/* RTPS:  */

if(operation.application.hasDataMap("KPIs from Stats to Physicals"))
	operation.application.getDataMap("KPIs from Stats to Physicals").execute(true)
    

//Smart Push Example
//if(operation.grid.hasSmartPush("&lt;Smart Push Name>"))
//  operation.grid.getSmartPush("&lt;Smart Push Name>").execute()</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="physical" obj_id="1" obj_type="1" name="PHYSICALS - MOVE PHYSICALS TO STORED MEMBERS - NIGHT"/><deployobject product="2" application="rpepm" plantype="stats" obj_id="2" obj_type="1" name="STATS - CALCULATE KPIS - NIGHT"/><deployobject product="2" application="rpepm" plantype="stats" obj_id="3" obj_type="1" name="STATS - PUSH KPIS TO PHYSICALS"/><deployobject product="2" application="rpepm" obj_id="1" obj_type="2" name="CALCULATE KPIS"/></deployobjects></HBRRepo>