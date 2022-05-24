var PROJECT = <PROJECT_ID>
var PUBSUB_TOPIC =  'workspace' ;
var API_KEY = <API_KEY>;
var CLIENT_ID = <user@..apps.googleusercontent.com>;
var CLIENT_SECRET = <...>;

function datefix(inpDate) {
  function pad(s) { return (s < 10) ? '0' + s : s; }
  var d = new Date(inpDate);
  dateString = [d.getFullYear(), pad(d.getMonth()+1), pad(d.getDate())].join('-')
  console.log(dateString)
  return dateString
}

function onFormSubmit(e) {
  Logger.log("myFunction triggerd on form submit");
  var patientData= {
     fname: e.namedValues['First Name'][0],
     mname: e.namedValues['Middle Name'][0],
     lname: e.namedValues['Last Name'][0],
     sex: e.namedValues['Sex'][0],
     dob: datefix(e.namedValues['Date of Birth (MM/DD/YYYY)'][0]),
     ht: e.namedValues['Height (inches)'][0],
     wt: e.namedValues['Weight (pounds)'][0],
     cn: e.namedValues['Contact Number (Area Code - Phone Number)'][0],
     ms: e.namedValues['Marital Status'][0],
     addrstreet: e.namedValues['Address - Street'][0],
     addrcity: e.namedValues['Address - City'][0],
     addrstate: e.namedValues['Address - State'][0],
     addrzip: e.namedValues['Address - ZipCode'][0],
     addrstart: e.namedValues['Address - Living since'][0],
     addruse: e.namedValues['Address - Use'][0],
     email: e.namedValues['Email Address'][0]
  };

  Logger.log('PATIENT DATA');
  Logger.log(patientData);
  Logger.log('PAYLOAD');
  payload = JSON.stringify(patientData);
  var attr = null;
  makePubSubRequest(PROJECT, PUBSUB_TOPIC, payload, attr)
}
