/**
 * Rule Name : ProjRep - Calling Data Maps
 * Purpose   : Execute a Data Map and optionally a Smart Push
 * Author    : Sneha Singh
 * Date      : <Add Date>
 */

// ----------------------------
// Step 1: Execute Data Map if it exists
// ----------------------------
def dataMapName = "KPIs from Stats to Physicals"
if (operation.application.hasDataMap(dataMapName)) {
    operation.application.getDataMap(dataMapName).execute(true)
    println("Data Map '${dataMapName}' executed successfully.")
} else {
    println("Data Map '${dataMapName}' not found.")
}

// ----------------------------
// Step 2: Optional Smart Push execution
// ----------------------------
// Example usage: replace <Smart Push Name> with actual Smart Push name
// def smartPushName = "<Smart Push Name>"
// if (operation.grid.hasSmartPush(smartPushName)) {
//     operation.grid.getSmartPush(smartPushName).execute()
//     println("Smart Push '${smartPushName}' executed successfully.")
// }
