SET EMPTYMEMBERSETS ON;
SET UPDATECALC ON;
SET FRMLRTDYNAMIC OFF;
SET COPYMISSINGBLOCK ON;
/*ENDCOMPONENT*/
FIX (/*DIM:Entity*/{Var_Dim_Entity},/*DIM:Period*/@Relative("YearTotal", 0),/*DIM:Scenario*/Actual,/*DIM:Version*/Final,/*DIM:View*/Month,/*DIM:Year*/&amp;ActYear,/*DIM:Plan Element*/"Calculated",/*DIM:Project*/@Relative({Var_Dim_Project}, 0))
  /*STARTCOMPONENT:SCRIPT*/
  Fix (@RELATIVE("Truck", 0), @RELATIVE("Trucks", 0)) 
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
  /*ENDCOMPONENT*/
ENDFIX
FIX (/*DIM:Entity*/{Var_Dim_Entity},/*DIM:Account*/@Relative("Physicals Measures", 0), @Relative("Physicals Costs", 0),/*DIM:Period*/@Relative("YearTotal", 0),/*DIM:Scenario*/Actual,/*DIM:Version*/Final,/*DIM:View*/Month,/*DIM:Year*/&amp;ActYear,/*DIM:Project*/{Var_Dim_Project})
  /*STARTCOMPONENT:SCRIPT*/
  Fix (@IDescendants("Mining_Equipment")) 
      /*Calculating truck Specific KPIs*/
      FIX ("Calculated") 
          CLEARBLOCK ALL;
      ENDFIX 
  Endfix 
  /*ENDCOMPONENT*/
ENDFIX</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="stats" obj_id="1" obj_type="1" name="STATS - CALCULATE KPIS"/></deployobjects></HBRRepo>