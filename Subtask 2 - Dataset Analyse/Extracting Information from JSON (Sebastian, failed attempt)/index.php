<?php
/* In order to handle larger amounts of data, a JSON streamer was included in the source code.
 * For this purpose, JsonMachine by Filip Halaxa has been included in this source code under the Apache2.0 license. 
 * To access the original repository of JsonMachine: https://github.com/halaxa/json-machine
 */
require_once('repos/json-machine/src/JsonMachine.php');
require_once('repos/json-machine/src/Parser.php');
require_once('repos/json-machine/src/Lexer.php');
require_once('repos/json-machine/src/StreamBytes.php');
require_once('repos/json-machine/src/JsonDecoder/ExtJsonDecoder.php');
require_once('repos/json-machine/src/JsonDecoder/DecodingResult.php');
require_once('repos/json-machine/src/JsonDecoder/JsonDecodingTrait.php');
require_once('repos/json-machine/src/Exception/PathNotFoundException.php');
require_once('repos/json-machine/src/Exception/JsonMachineException.php');

use JsonMachine\JsonDecoder\ExtJsonDecoder;
use JsonMachine\JsonMachine;

//get all files in the xampp directory
$files = scandir("C:/xampp/htdocs/");

//look for .json files
foreach ($files as $file)
{
	if (strpos($file, ".json") != false)
	{
		$filearray[] = $file;
	}
}

//go through every .json file
foreach ($filearray as $specificFile)
{
	unset($outputName);
	
	//extract the name of the .txt file
	$outputName[] = explode('.', $specificFile);
		
	/* old code that is no longer used, but kept for legacy reasons
	 * file_get_contents and json_decode have been abandoned for being too memory-consuming, but were used initially for most of the json files
	 */
	//read the data from the current file
	//$JSON = file_get_contents($specificFile);
	//decode the data into an object
	//$JSONobject = json_decode($JSON);

	//read the data from the current file
	$JSONobject = JsonMachine::fromFile($specificFile, '', new ExtJsonDecoder);
		
	//initialize the final output
	$text = "";
		
	//go through every argument
	foreach ($JSONobject as $name => $data) {
		foreach ($data as $test)
		{
			//add the conclusion to the final output
			$text = $text . " " . $test->conclusion;
				
			//go through all premises of the argument
			foreach ($test->premises as $premise)
			{
				//add every premise to the final output
				$text = $text . " " . $premise->text;	
			}
		}
	}
				
	//write the final output into a .txt file
	file_put_contents($outputName[0][0].".txt", $text);
}

print_r("All .json files were successfully processed!")
?>