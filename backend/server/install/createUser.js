db = connect( 'mongodb://localhost/admin' );
db.createUser( { user: "attackvector2",  pwd: "attackvector2", roles: [ "readWrite"] }, { } )