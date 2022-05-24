function makePubSubRequest(project, topic, data, attr) {
  var url = Utilities.formatString("https://pubsub.googleapis.com/v1/projects/%s/topics/%s:publish", project, topic);
  Logger.log(url)
  Logger.log(data)
  var body = {
    messages: [
      {
        attributes: attr,
        data: Utilities.base64Encode(data)
      }
    ]
  };

  var response = UrlFetchApp.fetch(url, {
    method: "POST",
    contentType: 'application/json',
    muteHttpExceptions: true,
    payload: JSON.stringify(body),
    headers: {
      Authorization: 'Bearer ' + ScriptApp.getOAuthToken()
    }
  });

  var result = JSON.parse(response.getContentText());
  // Put error code 
  var message = JSON.stringify(result);
  Logger.log("Message");
  Logger.log(message);
  return {
    log: message
  }
}
