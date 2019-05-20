var express = require('express');
var socket = require('../util/socket');
var router = express.Router();
var path = require('path');

socket.init();

/* GET home page. */
router.get('/index', function(req, res, next) {
  res.sendFile(path.join(__dirname, "../html/index.html"));
});

module.exports = router;
