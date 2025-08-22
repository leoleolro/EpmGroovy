
// Project_Status and Project_Type.

def startTime = currentTimeMillis()

/* Define cube where the calculations will take place */
Cube cube = operation.getApplication().getCube('ProjRep')

/* Define the data grid */
def dataGrid = cube.flexibleDataGridDefinitionBuilder()

/*Populate the Datagrid*/
dataGrid.setPov('BegBalance','No Scenario','No View','No Year','No Line Item','Load','Working') //set POV members
dataGrid.addColumn('Project Status') //set column members
dataGrid.addColumn('Project Type')
dataGrid.addColumn('Cost Tracking') 
dataGrid.addRow('ILvl0Descendants(All Entities)','ILvl0Descendants(All Projects)') //set row members

/*Grid suppression*/
dataGrid.setSuppressMissingRows(true)
dataGrid.setSuppressMissingBlocks(true)
dataGrid.setSuppressMissingSuppressesZero(true)

//Define Project attribute variables.
def strProjectType
def strProjectStatus
def strCostTracking

Dimension dimProject = operation.application.getDimension("Project")

// Load a data grid from the specified grid definition and cube
cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->
 
 grid.dataCellIterator("Project Status").each { cell ->
      if(cell.isMissing())	
     	{
        strProjectStatus = "&lt;None>"
        }
      else {
      	strProjectStatus = cell.formattedValue
      }
     
     Member mbr_Project = operation.application.getDimension("Project").getMember(cell.getMemberName("Project").toString())
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Project_Status":strProjectStatus] as Map))
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strProjectStatus) 
                   }
                  
 grid.dataCellIterator("Project Type").each { cell ->
     if(cell.isMissing())	
     	{
        strProjectType = "&lt;None>"
        }
      else {
      	strProjectType = cell.formattedValue
      }	
	 
     Member mbr_Project1 = operation.application.getDimension("Project").getMember(cell.getMemberName("Project").toString())
     mbr_Project1 = dimProject.saveMember(mbr_Project1.toMap() &lt;&lt; (["Project_Type":strProjectType] as Map))
                   }
                   
 grid.dataCellIterator("Cost Tracking").each { cell ->     
      if(cell.isMissing())	
     	{
        strCostTracking = "&lt;None>"
        }
      else {
      	strCostTracking = cell.formattedValue
      }
     
     Member mbr_Project = operation.application.getDimension("Project").getMember(cell.getMemberName("Project").toString())
     mbr_Project = dimProject.saveMember(mbr_Project.toMap() &lt;&lt; (["Cost_Tracking":strCostTracking] as Map))
     println(cell.getEntityName() + " : " + mbr_Project.toString() + " : " + strCostTracking) 
                   }                  
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - SMARTLIST TO ATTRIBUTE TRANFORMATION"/></deployobjects></HBRRepo>