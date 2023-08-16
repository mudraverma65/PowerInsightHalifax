Project for CSCI3901 - Software Development Concepts

Create a class called “PowerService” that lets us manage information about the power service and reports on that power service. The class can use internal data structures, databases, or files to store information.

The information managed by the class should survive between execution of programs that use the PowerService class. In a deployment environment (beyond the scope of this project), the central power utility would record postal code information and distribution hub information as these are more static pieces of information. Employees in the field would have an app on their phones that would report damage or repairs to hubs to the central power utility to immediately be included in any reporting or planning by the PowerService class.

Here is the minimum information that you will need for the project.

Postal codes describe the province to the class. Each postal code has a unique string identifier. In Nova Scotia, that identifier is a 6 character string that alternates letters and digits and begins with a letter. The postal code system is hierarchical, meaning that if we truncate the postal code to fewer characters then we are capturing the idea of a grouping of smaller postal codes into one larger area. For any postal code that we enter into the system, we will want to have 
- The postal code identifier
- The number of people living in the postal code (from the last government census)
- The area covered by the postal code.
  
Each power distribution hub is located in a community in the province. For each hub, we will know:
- An alphanumeric identifier for the hub
- The coordinate location of the hub in the world in UTM coordinates. UTM coordinates
  look like regular (x, y) coordinates where each unit is 1 metre and we imaging that the
  local world is a flat plane, so we don’t worry about the curvature of the earth, which is
  fine for the province.
- The set of postal codes that are served by the hub. One hub can serve multiple postal
  codes and one postal code can be served by multiple hubs.
- The estimate of the number of hours needed to repair the current hub, if there is any
  damage.
  
As a hub is repaired, we want a log of when the repair was done, over how many hours and by which trained employee in the company. In theory, repairs can be partial repairs, and your system should be ready to handle that.

Ultimately, we want you to answer the following questions for the power company:
- How many people are without service?
- What is the most damaged region?
- Which hubs should we fix first to have the greatest impact in getting people back into
  service?
- Under perfect conditions, at what rate do people get service back?
- Provide a plan for one vehicle to service hubs.
- Which areas are most underserviced by the company for which we need to improve?
  
Your task is to create classes that will gather the information for this system and resolve these queries.