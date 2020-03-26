<?php

//Get params
$reminderId 		= $_GET["id"];
$reminderText 		= $_GET["text"];
$reminderStatus 	= $_GET["status"];
$reminderScheduled 	= $_GET["scheduled"];
$reminderPriority 	= $_GET["priority"];

//Get data
$jsonData = file_get_contents("data.json");
$data = json_decode($jsonData, true);

//Find a usableId
for($i = 0; $i<count($data["groups"]); $i++){
	$group = $data["groups"][$i];
	for($j = 0; $j<count($group["reminders"]); $j++){
		$reminder = $group["reminders"][$j];
		if($reminderId == $reminder["id"]){
			//Update values

			if($reminderText){
				$data["groups"][$i]["reminders"][$j]["text"] = $reminderText;
			}

			if($reminderStatus || $reminderStatus === "0"){
				$data["groups"][$i]["reminders"][$j]["status"] = intval($reminderStatus);
			}

			if($reminderScheduled){
				if ($reminderScheduled == "null"){
					$data["groups"][$i]["reminders"][$j]["scheduled"] = "";
				}else{
					$data["groups"][$i]["reminders"][$j]["scheduled"] = $reminderScheduled;
				}
			}

			if($reminderPriority){
				$data["groups"][$i]["reminders"][$j]["priority"] = $reminderPriority;
			}
		}		
	}
}

//Encode and return it
$jsonData = json_encode($data);
file_put_contents("data.json", $jsonData);
header('Content-Type: application/json');
echo $jsonData;





