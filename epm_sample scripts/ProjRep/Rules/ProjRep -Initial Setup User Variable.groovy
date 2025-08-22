 /**/
String userName = operation.user.getName();
print userName

/*** Set user variables based on username or default ****/
Map userVarMap = [
    1: [username:'default', 
     		'Account-Accounts':'Project Raw Cost',
            'Asset-Equipment Class':'DRI280003', 
            'Account-Physicals Measures':'BOOM_P2', 
            'Period-UV_Period':'Jul',
            'Year-UV_Year':'FY25',
            'Version-Version':'Working'
            ]
]
    
String userVarString = null
if (userVarString == null) {
        userVarString = userVarMap.find { it.value.username == 'default' }?.value // find default entry if user not found
    }
 
Map userMap=[:]
    userMap += userVarString.replaceAll('\\{|\\}', '').split(',').collectEntries { entry ->
        def pair = entry.split('=')
        [(pair.first().trim()): pair.last().trim()]
       } // convert userVarString entry back to Map object type
 
for (i in userMap) {
    if(i.key == 'username')
    continue
    String mapKey = i.key.toString()
    def dimPair = mapKey.split('-')
    def dimName = dimPair.first().trim()
    def userVar = dimPair.last().trim()
    String userVarValue = i.value.toString()
 
    Dimension appDim = operation.application.getDimension(dimName)
    UserVariable appUserVar = operation.application.getUserVariable(userVar)
    Member appUserVarMember = appDim.getMember(userVarValue)
    String status = operation.application.setUserVariableValue(appUserVar,appUserVarMember)
}
</script></rule></rules><components/><deployobjects><deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP -INITIAL SETUP USER VARIABLE"/></deployobjects></HBRRepo>