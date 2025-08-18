// Cost_Tracking.

long startTime = currentTimeMillis()

/* Define cube where the calculations will take place */
Cube cube = operation.getApplication().getCube('ProjRep')

/* Define the data grid */
FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder()

/*Populate the Datagrid*/
dataGrid.setPov('BegBalance','No Scenario','No View','No Year','No Line Item','Load','Working') //set POV members
dataGrid.addColumn('Cost Tracking', 'Project Status', 'Project Type') 
dataGrid.addRow('ILvl0Descendants(All Projects)','ILvl0Descendants({Var_Dim_Entity})') //set row members

/*Grid suppression*/
dataGrid.setSuppressMissingRows(true)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)

//Define Project attribute variables.

def strCostTracking
def strProjectStatus
def strProjectType

Dimension dimProject = operation.application.getDimension("Project")

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
 grid.dataCellIterator('Project Status').each { cell ->     
  if (cell.crossDimCell('Project Status').formattedValue == '2 Active'){
//Applying Cost Tracking Attribute
    if (cell.crossDimCell('Cost Tracking').formattedValue == 'Yes'){
    	strCostTracking="Yes"
    }else if (cell.crossDimCell('Cost Tracking').formattedValue == 'No'){
    	strCostTracking="No"
    }else{
    	strCostTracking = "&lt;None>"
                  }
//Applying the Project Type    
    if (cell.crossDimCell('Project Type').formattedValue == 'Operational Project'){
    strProjectType= "Operational Project"
    } else if (cell.crossDimCell('Project Type').formattedValue == 'Capital Project'){
    strProjectType= "Capital Project"
    }
    
//Updating the Project Status    
    if (cell.crossDimCell('Project Status').formattedValue == 'Draft'){
    strProjectStatus = "Draft"
    } else if (cell.crossDimCell('Project Status').formattedValue == '2 Active'){
    strProjectStatus = "2 Active"
    }else if (cell.crossDimCell('Project Status').formattedValue == 'Submitted'){
    strProjectStatus = "Submitted"
    }else if (cell.crossDimCell('Project Status').formattedValue == '3 Pending Close'){
    strProjectStatus = "Pending_Close"
    }else if (cell.crossDimCell('Project Status').formattedValue == '4 Closed'){
    strProjectStatus = "4 Closed"
    }
   Member mbr_Project = operation.application.getDimension("Project").getMember(cell.getMemberName("Project").toString())
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Cost_Tracking":strCostTracking] as Map))
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Project_Type":strProjectType] as Map))
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Project_Status":strProjectStatus] as Map))
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strCostTracking)
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strProjectType)
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strProjectStatus)   
   }else{
   strProjectStatus = cell.crossDimCell('Project Status').formattedValue
   Member mbr_Project = operation.application.getDimension("Project").getMember(cell.getMemberName("Project").toString())
   mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Project_Status":strProjectStatus] as Map))
   println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strProjectStatus)  
   }        
    
  /*  Member mbr_Project = operation.application.getDimension("Project").getMember(cell.getMemberName("Project").toString())
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Cost_Tracking":strCostTracking] as Map))
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Project_Type":strProjectType] as Map))
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Project_Status":strProjectStatus] as Map))
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strCostTracking)
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strProjectType)
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strProjectStatus)*/   
	}
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - COST TRACKING ATTRIBUTE UPDATE"/></deployobjects></HBRRepo>