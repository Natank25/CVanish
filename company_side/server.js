var express = require('express'),
    async = require('async'),
    pg = require("pg"),
    path = require("path"),
    cookieParser = require('cookie-parser'),
    bodyParser = require('body-parser'),
    methodOverride = require('method-override'),
    app = express(),
    server = require('http').Server(app),
    io = require('socket.io')(server);

var db_uri = "postgres://" + process.env['POSTGRES_USER']
    + ':' + process.env['POSTGRES_PASSWORD']
    + "@" + process.env['POSTGRES_HOST']
    + ':' + process.env['POSTGRES_PORT']
    + '/' + process.env['POSTGRES_DB'];

io.sockets.on('connection', function (socket) {

  socket.emit('message', { text : 'Welcome!' });

    socket.on('subscribe', function (data) {
    socket.join(data.channel);
  });
});

async.retry(
  {times: 1000, interval: 1000},
  function(callback) {
      const client = new pg.Client(db_uri);
      client.connect().then(() => {
        callback(undefined, client);
      }).catch((err) => {
        console.error("Waiting for db", err);
        callback(err, undefined);
      });
  },
  function(err, client) {
    if (err) {
      return console.error("Giving up");
    }
    console.log("Connected to db");
    getCV(client);
  }
);

function getCV(client) {
  client.query('SELECT first_name, last_name, user_id FROM applications', [])
  .then((result) => {
    var votes = collectVotesFromResult(result);
    io.sockets.emit("scores", JSON.stringify(votes));
    setTimeout(function() {getCV(client) }, 1000);
  }).catch((err) => {
    console.error("Error performing query: " + err);
    setTimeout(function() {getCV(client) }, 1000);
  });
}

function collectVotesFromResult(result) {
  var votes = [];

  result.rows.forEach(function (row) {
    votes.concat({user_id:row.user_id, first_name:row.first_name, last_name:row.last_name});
  });

  return votes;
}

app.use(cookieParser());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: true
}));
app.use(methodOverride('X-HTTP-Method-Override'));
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
  next();
});

app.use(express.static(__dirname + '/views'));

app.get('/', function (req, res) {
  res.sendFile(path.resolve(__dirname + '/views/site_company.html'));
});

server.listen(80, function () {
  var port = server.address().port;
  console.log('App running on port ' + port);
});
