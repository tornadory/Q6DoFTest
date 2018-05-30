//defined: component, node, scene, transform, globals
this.onStart = function()
{
	//window.updateController = updateController;
}

this.onUpdate = function(dt)
{
	if(!window.sdof){
		return;
	}
	var ret = window.sdof.get6Dof();
    if(ret != ""){
	  updateController(ret)
	}
	node.scene.refresh();
}

function updateController(data){
	var pos = vec3.create();
	var R = quat.create();

    var jsonDT = JSON.parse(data);
	console.log("got data : " + data);
	pos[0] = 0 - parseFloat(jsonDT.position.x);
	pos[1] = 0 - parseFloat(jsonDT.position.y);
	pos[2] = 0 - parseFloat(jsonDT.position.z);
	R[0] = parseFloat(jsonDT.rotation.x);
	R[1] = parseFloat(jsonDT.rotation.y);
	R[2] = parseFloat(jsonDT.rotation.z);
	R[3] = 0 - parseFloat(jsonDT.rotation.w);

	node.transform.position = pos;
	node.transform.rotation = R;
}