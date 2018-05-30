//defined: component, node, scene, transform, globals
this.hasgamepad = false;
this.gamepad = null;

function updateControllerN(data){
  //console.log("update controller data ", data);
  var jsonDT = JSON.parse(data);

  if(parseInt(jsonDT.buttons) == 2){
	  console.error("button is 0x2");
  }

  if(parseInt(jsonDT.buttons) == 3){
	  console.error("button is 0x3"); //button is clicked( I forgot which buttons)
  }

  if(parseInt(jsonDT.buttons) == 4){
	  console.error("button is 0x4");
  }

  //console.log(jsonDT.rotation.yaw);
  //console.log(jsonDT.rotation.pitch);
  //console.log(jsonDT.rotation.roll);
  var euler = vec3.create();
  var quater = quat.create();

  euler[0] = 0 - parseFloat(jsonDT.rotation.pitch); //
  euler[1] = parseFloat(jsonDT.rotation.yaw);       //
  euler[2] = 0 - parseFloat(jsonDT.rotation.roll);  //

  console.log("euler is ", euler);
  //node.transform.setRotationFromEuler(euler);

  //node.transform.rotateX(euler[0]);
  //node.transform.rotateY(euler[1]);
  //node.transform.rotateZ(euler[2]);

  //quat.fromEuler
  quat.fromEuler(quater, euler);
  //console.log("quater is ", quater);
  node.transform.rotation = quater;
}

this.onStart = function()
{
	window.updateController = updateControllerN;

	//window.addEventListener("gamepadButtonDown", function(e) {
	//	console.log("gamepad button down with ", e);
	//	}, false);
	//window.addEventListener("WebkitGamepadButtonDown", function(e) {
	//	console.log("webkit gamepad button down with ", e);
	//	}, false);

}

this.onUpdate = function(dt)
{
	if(!window.sdof){
		return;
	}
	var ret = window.sdof.get3dofController();
    if(ret != ""){
      //var jsonDT = JSON.parse(ret);
	  updateControllerN(ret)
	}


	//var x_axis = 0;
	//var y_axis = 0;
	//node.scene.refresh();

	//if(!this.hasgamepad){
	//	if(this.gamepad){
	//		console.log("yes, gamepad is ready");
	///		x_axis = LS.Input.getGamepadAxis(0, "LX");
	//		y_axis = LS.Input.getGamepadAxis(0, "LY");
	//		this.hasgamepad = true;
	//	}else{
	//		//console.log("still no ???");
	//	}
	//}

	//if(LS.Input.Keyboard["UP"]){
	//	console.log("UP");
	//}

	//if(LS.Input.isGamepadButtonPressed(0)){
	//	console.log("button 0 clicked");
	//}

	//if(LS.Input.isGamepadButtonPressed(1)){
	//	console.log("button 1 clicked");
	//}

	//if(LS.Input.isGamepadButtonPressed(2)){
	//	console.log("button 2 clicked");
	//}

	//if(LS.Input.isGamepadButtonPressed(3)){
	//	console.log("button 3 clicked");
	//}

	//gl.keys[key] = e.type == "keydown";
	//gl.keys[e.keyCode] = e.type == "keydown";
	//

}

//this.onGamepadConnected = function()
//{
//	console.error("game pad connected!!!");
//
//	this.gamepad = LS.Input.getGamepad(0);
	//console.log("gamepad: ", this.gamepad);
//	if(this.gamepad){
//		console.log("yes, gamepad is ready");
//		x_axis = LS.Input.getGamepadAxis(0, "LX");
//		y_axis = LS.Input.getGamepadAxis(0, "LY");
//		this.hasgamepad = true;
//	}else{
//		console.log("still no ???");
//	}
//}

//this.onKey = function(e){
//	console.log('onkey event: ', e);
//}

//this.onKeyDown = function(e){
//	console.log('on key down', e);
//	if(e.code == "KeyA"){
//		node.transform.rotateX(0.1);
//	}
//
//	if(e.code == "KeyB"){
///		node.transform.rotateY(0.1);
//	}
//
//	if(e.code == "KeyC"){
//		node.transform.rotateZ(0.1);
//	}
//}

//this.onGamepad = function(e){
//	console.log('onGamepad with event ', e);
//}

//this.onButtonDown = function(e){
//	console.log('on button down', e);
//}

//this.onFileDrop = function(e){
//	console.log('on file drap', e);
//}

//this.onButtonUp = function(e){
//	console.log('on button up', e);
//}

