<?php

//Get params
$groupName = $_GET["name"];

//Get data
$jsonData = file_get_contents("data.json");
$data = json_decode($jsonData, true);
$groups = $data["groups"];

//Search an id
$maxId = 0;
foreach($groups as &$group){
	if($group["id"] > $maxId){
		$maxId = $group["id"];
	}
}
$newId = $maxId + 1;

//Update data
array_push($groups, array(
	"id" => $newId,
	"name" => $groupName,
	"reminders" => array()
));
$data["groups"] = $groups;

//Encode and return it
$jsonData = json_encode($data);
file_put_contents("data.json", $jsonData);
header('Content-Type: application/json');
echo $jsonData;





