var AuthorizationsPage = require('./authorizations');
var AuthorizationsApplicationPage = require('./authorizationsApplication');
var AuthorizationsAuthorizationPage = require('./authorizationsAuthorization');
var AuthorizationsGroupPage = require('./authorizationsGroup');
var AuthorizationsGroupMembershipPage = require('./authorizationsGroupMembership');
var AuthorizationsUserPage = require('./authorizationsUser');

module.exports = new AuthorizationsPage();
module.exports.application = new AuthorizationsApplicationPage();
module.exports.authorization = new AuthorizationsAuthorizationPage();
module.exports.group = new AuthorizationsGroupPage();
module.exports.groupMembership = new AuthorizationsGroupMembershipPage();
module.exports.user= new AuthorizationsUserPage();
