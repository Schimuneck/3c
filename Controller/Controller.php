<?php
/**
 * Including and configuring database connection
 */
#include_once 'MySQL.php';
#$database = '3c';
#$username = 'root';
#$password = 'root';

/**
 * Receiving message and decoding json
 * 
 * json example = {'message': "teste123"}
 */
#$data = json_decode(file_get_contents('php://input'), true);

#if(isset($data)){
/**
 * Initialization and insertion into database
 */
#$db = new MySQL($database, $username, $password);

#$table = 'responsavel';
#$arrayToInsert = [
#	'id' => $data['responsavel']['id'],
#	'nome' => $data['responsavel']['nome'],
#];

#$db->insert($table, $arrayToInsert);

#$id_responsavel = $db->lastInsertID();


$response = array("link"=>"http://10.42.0.1:12345/videos/h264.1000.mp4");
echo json_encode($response);
#} else {
#$response = array("link"=>0);
#echo json_encode($response);
#}

