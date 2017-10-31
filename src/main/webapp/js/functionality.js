$(document).ready(function() {

	if($('#eval_txt').val() != 'null'){
		$('#tab8_id').addClass('current');
		$('#tab-8').addClass('current');
		
		$('#tab-1').removeClass('current');
		$('#tab1_id').removeClass('current');
	}else{
		$('#tab1_id').addClass('current');
		$('#tab-1').addClass('current');
		
		$('#tab-8').removeClass('current');
		$('#tab8_id').removeClass('current');
	}
	let cyEvent;
	let cyGeneral;
	
	  w3.includeHTML();
		$('ul.tabs li').click(function(){
			
			var tab_id = $(this).attr('data-tab');

			$('ul.tabs li').removeClass('current');
			$('.tab-content').removeClass('current');

			$(this).addClass('current');
			$("#"+tab_id).addClass('current');
			
			renderTimeLineGraph();
			renderGanttGraph()
		})
		
	var availableTags = [
        "Show Characters with",
      ];


//    $("#generate_btn").click(function(event) {
//    	const mode = $('#selectMode option:checked').val();
//    	if(mode === "story" || mode === "interview"  || mode === "sample" )
//    		serverCall('sampleaddress');
//	})


	function serverCall(inputAddress) {debugger;
    	try {
    		$.ajax({
    			url : 'MainServlet',
    			data : {
    				action : 'generate',
    				story : $('#txtInput').val(),
    				mode: $('#selectMode option:checked').val(),
    	        	removeProfilesWitoutInfo: $('#chk-removeProfilesWitoutInfo').val(),
    	        	removeProfilesWithFreq1 : $('#chk-removeProfilesWithFreq1').val(),
    	        	removeEventsWithoutLocationAndObejct : $('#chk-removeEventsWithoutLocationAndObejct').val(),
    	        	removeEventsWithNoCharacterObject : $('#chk-removeEventsWithNoCharacterObject').val(),
    	        	coreferenceWithCoreNLP : $('#chk-coreferenceWithCoreNLP').val(),
    	        	useQuote : $('#chk-useQuote').val()
    			},
    			success : function(responseText) {
    				alert(JSON.parse(responseText).message);
    			},
    			error: function(err){
    				alert(err);
    			}
    		});
    	} catch (err) {
    		alert(err);
    	}
    	}

	
	function generateEventGraph(){
		let eventFileName;
		
		const mode = $('#selectMode option:checked').val();
		if (mode === "story") {
			eventFileName = "events_story.json";
		}
		else {
			eventFileName = 'events_'+mode+'.json';
		}
		
		
		try {
			Promise.all([
			    fetch(eventFileName, {mode: 'no-cors'})
			    .then(function(res) {
			      return res.json()
			    }),
			    fetch('cy-style-event.json', {mode: 'no-cors'})
				.then(function(res) {
				return res.json()
				},$("#header h1").text("please wait..."))]).then(function (dataArray){
					
					
			    	let events = dataArray[0];
			    	let finalStyle = dataArray[1];
			    	let finaldata = [];
			    	let id=1;
			    	events.forEach((event)=>{
			    		const verb = event.lemmatizedVerb;
			    		const subject = event.subject;
			    		const object = event.object;
			    		const locations = event.locations;
			    		const times = event.times;
			    		
			    		
			    		const tooltip = '' ;
			    		const node ={
			    			  "data": {
			    				"id": id,
			    			   	"verb": verb,
			    			    "subject": subject,
			    			    "object": object,
			    			    "locations": locations,
			    				"times": times,
			    				"tooltip": tooltip,
			    				"content":subject + '\n\n' + verb + '\n\n' + ((object !="") ? object : '[]') 
			    			  }, 
			    			  "group": "nodes",
			    			  "classes": (id == 1) ? "first" : "event"
			    		};
			    		if(id > 1){
			    			const edge ={
					      			  "data": {
					      			    "source": (id-1).toString(),
					      			    "target": id,
					      			    "id": (id-1).toString() + '-' + id 
					      			  },
					      			  "group": "edges",
					      			  "classes": "event-edge"
					      		};
					      		finaldata.push(edge);
			    		}
			    		finaldata.push(node);
			    		if(locations != ""){
			    			const loc = {
			    					"data": {
					    				"id": id + 'loc',
					    				"content":locations 
					    			  }, 
					    			  "group": "nodes",
					    			  "classes": 'location'	
			    			}
			    			const locEdge = {
			    					"data": {
					      			    "source": id,
					      			    "target": id + 'loc',
					      			    "id": id + '-' + id + 'loc' 
					      			  },
					      			  "group": "edges",
					      			  "classes" : "one-dir"
					      				  
			    			}
			    			finaldata.push(loc);
			    			finaldata.push(locEdge);
			    		}
			    		if(times != ""){
			    			const time = {
			    					"data": {
					    				"id": id + 'time',
					    				"content":times 
					    			  }, 
					    			  "group": "nodes",
					    			  "classes": 'location'	
			    			}
			    			const timeEdge = {
			    					"data": {
					      			    "source": id,
					      			    "target": id + 'time',
					      			    "id": id + '-' + id + 'time' 
					      			  },
					      			  "group": "edges",
					      			  "classes" : "one-dir"
					      				  
			    			}
			    			finaldata.push(time);
			    			finaldata.push(timeEdge);
			    		}
			    		id = id +1;
			    	});
			    	
			    	
			        cyEvent = window.cy = cytoscape({
			            container: document.getElementById('cy-event'),
			            pan: { x: 0, y: 0 },
			            
			            zoomingEnabled: true,
			            userZoomingEnabled: true,
			            layout: {
			              name: 'cose',
			              fit: true,
			            },
			            style: finalStyle,
			            elements: finaldata

			          });
			        

			        cyEvent.ready(function(){
			        	
			        	var defaults = {
			        			  minZoom: 0.5,
			        			  maxZoom: 2.0, // max zoom level
			        			  sliderHandleIcon: 'fa fa-minus',
			        			  zoomInIcon: 'fa fa-plus',
			        			  zoomOutIcon: 'fa fa-minus',
			        			  resetIcon: 'fa fa-expand'
			        			};

			        			cyEvent.panzoom( defaults );
			        			cyEvent.center();
			        });

				})} catch (err) {
			console.log(err);
				}

	}

	
	document.getElementById("btn-file").onclick = function () {
		document.getElementById("upload_btn").disabled = true;  
		this.value = null;
	};
	
	document.getElementById("btn-file").onchange = function () {
		document.getElementById("upload_btn").disabled = false;    
	};
	
	$("#btnExport").click(function(event) {
		const chart = $('#selectChart option:checked').val();
		var png64;
		
		if(chart == "event"){
			png64 = cyEvent.jpg({full:true});
		}else{
			png64 = cyGeneral.jpg({full:true});
		}
		$('#imgExport').attr('src', png64);
	});

	
	$("#btnRenderEvent").click(function(event) {
		generateEventGraph();
	});
	
	$("#btnRenderGraph").click(function(event) {
		renderProfileGraph(true, false, false, false, true, 'cy');
	});
	
	$("#btnRenderWholeGraph").click(function(event) {
		const loc = $('#input_location').is(":checked");
		const t = $('#input_Time').is(":checked");
		const adj = $('#input_adj').is(":checked");
		const verb = $('#input_Verb').is(":checked");
		const pers = $('#input_personality').is(":checked");
		
		renderProfileGraph(loc, t, adj, verb, pers, 'cyCustom');
	});
	
	
	$("#btnCustomQueryGraph").click(function(event){
		var cy = window.cy;
		
		let query = $("#input_CustomSearchText").val();
		if(query.indexOf('Show Characters with') >= 0){
			query = query.substring('Show Characters with'.length, query.length).trim();
		}
		cy.elements().forEach((ele)=>{
				ele.hide();		
		});
	
		
		const locNodes = cy.$("[id$='"+query+"']");
		locNodes.show();
		const firstLevel = locNodes.neighborhood();
		firstLevel.show();
		const secondLevel = firstLevel.neighborhood();
		secondLevel.show();
	
	});
	
	$("#btnQueryGraph").click(function(event) {
		var cy = window.cy;
		cy.elements().forEach((ele)=>{
				ele.hide();		
		});
	
		
		var val = $("#input_searchText").val()
		if(val != "" && cy.$(val)){
			cy.$(val).show();
		}	
	});
	
	$("#btnFilterGraph").click(function(event) {
		var cy = window.cy;
		cy.elements().forEach((ele)=>{
			if(ele.hidden()){
				ele.show();		
			}
		});
	
		
		if(!$('#input_location').is(":checked")){
			cy.$('[name="locations"]').forEach((ele) =>{
				ele.hide();	
				const edges = cy.$('[source="'+ele.data('id')+'"]');
				edges.forEach((edge) =>{
					cy.$('[id="' + edge.data('target') + '"]').hide();
				})
			});
			
		}
			
		if(!$('#input_Time').is(":checked")){
			cy.$('[name="temporals"]').forEach((ele)=>{
				ele.hide();	
				const edges = cy.$('[source="'+ele.data('id')+'"]');
				edges.forEach((edge) =>{
					cy.$('[id="' + edge.data('target') + '"]').hide();
				})
			});
		}
		
		
		if(!$('#input_Verb').is(":checked")){
			cy.$('[name="verbs"]').forEach((ele) =>{
				ele.hide();	
				const edges = cy.$('[source="'+ele.data('id')+'"]');
				edges.forEach((edge) =>{
					cy.$('[id="' + edge.data('target') + '"]').hide();
				})
			});
		}
		
		
		if(!$('#input_personality').is(":checked")){
			cy.$('[name="personality"]').forEach((ele)=>{
				ele.hide();	
				const edges = cy.$('[source="'+ele.data('id')+'"]');
				edges.forEach((edge) =>{
					cy.$('[id="' + edge.data('target') + '"]').hide();
				})
			});	
		}
		
		
		if(!$('#input_adj').is(":checked")){
			cy.$('[name="adjs"]').forEach((ele)=>{
				ele.hide();	
				const edges = cy.$('[source="'+ele.data('id')+'"]');
				edges.forEach((edge) =>{
					cy.$('[id="' + edge.data('target') + '"]').hide();
				})
			});
		}
	});
	

	
function renderProfileGraph(location, time, adj, verb, pers, elementId) {
	
	let proileFileName;
	let relationFileName;
	const mode = $('#selectMode option:checked').val();
	if (mode === "story") {
		proileFileName = "profiles_story.json";
		relationFileName = "relations_story.json";
	}
	else {
		proileFileName = 'profiles_'+mode+'.json';
		relationFileName = "relations_"+mode+".json";
	}
	
	
	try {
		Promise.all([
		    fetch(proileFileName, {mode: 'no-cors'})
		    .then(function(res) {
		      return res.json()
		    }),
		    fetch(relationFileName, {mode: 'no-cors'})
		    .then(function(res) {
		      return res.json()
		    }),fetch('cy-style.json', {mode: 'no-cors'})
			.then(function(res) {
			return res.json()
			},$("#header h1").text("please wait..."))]).then(function (dataArray){
				
				
		    	let profiles = dataArray[0];
		    	let relations = dataArray[1];
		    	let finalStyle = dataArray[2];
		    	let finaldata = []
		    	profiles.forEach((profile)=>{
		    		const id = profile.name;
		    		const name = profile.name;
		    		const freq = profile.frequency;
		    		const verbs = profile.verbs;
		    		let classes;
		    		let maxSentiment = 0;
		    		if(maxSentiment < profile.negativeSentimentCount){
		    			maxSentiment = profile.negativeSentimentCount;
		    			classes = 'negativeSentiment';
		    		}
		    		if(maxSentiment < profile.positiveSentimentCount){
		    			maxSentiment = profile.positiveSentimentCount;
		    			classes = 'positiveSentiment';
		    		}
		    		if(maxSentiment < profile.veryPositiveSentimentCount){
		    			maxSentiment = profile.veryPositiveSentimentCount;
		    			classes = 'veryPositiveSentiment';
		    		}
		    		if(maxSentiment < profile.veryNegativeSentimentCount){
		    			maxSentiment = profile.veryNegativeSentimentCount;
		    			classes = 'veryNegativeSentiment';
		    		}
		    		
		    		const tooltip = '<b>negativeSentiment:</b>' +  profile.negativeSentimentCount + '<br/>' + 
		    		'<b>positiveSentiment:</b>' +  profile.positiveSentimentCount + '<br/>' + 
		    		'<b>veryPositiveSentiment:</b>' +  profile.veryPositiveSentimentCount + '<br/>' + 
		    		'<b>veryNegativeSentiment:</b>' +  profile.veryNegativeSentimentCount ;
		    		const node ={
		    			  "data": {
		    			   	"id": id,
		    			    "name": name,
		    			    "score": freq,
		    			    "query": true,
		    				"gene": true,
		    				"tooltip": tooltip
		    			  }, 
		    			  "group": "nodes",
		    			  "classes": classes
		    		};
		    		finaldata.push(node);

//		    		if($('#input_location').is(":checked")){
		    		if(location){
		    			const locArr = addLocations(profile);
		    			locArr.forEach((loc) =>{
			    			finaldata.push(loc);	
			    		});
		    		}
//		    			
//		    		if($('#input_Time').is(":checked")){
		    		if(time){
		    			const tempArr = addTemporals(profile);
		    			tempArr.forEach((temp) =>{
			    			finaldata.push(temp);	
			    		});
		    			
		    		}
//		    		
//		    		
//		    		if($('#input_Verb').is(":checked")){
		    		if(verb){
		    			const verbArr = addVerbs(profile);
		    			verbArr.forEach((verb) =>{
			    			finaldata.push(verb);	
			    		});
		    		}
		    		
		    		
//		    		if($('#input_personality').is(":checked")){
		    		if(pers){
		    			const persArr = addPersonalities(profile);
			    		persArr.forEach((pers) =>{
			    			finaldata.push(pers);	
			    		});	
		    		}
		    		
		    		
//		    		if($('#input_adj').is(":checked")){
			    	if(adj){
		    			const ajArr = addAdjc(profile);
			    		ajArr.forEach((adj) =>{
			    			finaldata.push(adj);	
			    		});		
		    		}
		    		
			        $( "#input_CustomSearchText" ).autocomplete({
			            source: availableTags
			         });
		    	});
		    	relations.forEach((relation) => {
		    		const profileNames = relation.profileNames;
		    		const frequency = relation.frequency / 10;
		    		let tooltip = "";
		    		const relationLabels = relation.typeName;
		    		for(label in relationLabels){
		    			tooltip += " " + relationLabels[label];
		    		}
		    		for(i=0; i<profileNames.length;i++){//with this nested loop we can find all the combinations of  3 profiles
		    			const firstPName =  profileNames[i];
		    			for(j=i; j<profileNames.length;j++){
		    				const secondPName = profileNames[j];
		    				if(firstPName != secondPName){
		    					const edge ={
		    			      			  "data": {
		    			      			    "source": firstPName,
		    			      			    "target": secondPName,
		    			      			    "weight": frequency,
		    			      			    "label": "relation",
		    			      			    "tooltip": tooltip,
		    			      			    "id": firstPName + secondPName 
		    			      			  },
		    			      			  "group": "edges",
		    			      			  "classes": ""
		    			      		};
		    			      		finaldata.push(edge);
		    				}
		    			}
		    		}
		    	});
		    	
		        cyGeneral = window.cy = cytoscape({
		            container: document.getElementById(elementId),
		            pan: { x: 0, y: 0 },
		            minZoom: 1.0,
		            maxZoom: 8.0,
		            zoomingEnabled: true,
		            userZoomingEnabled: true,
		            layout: {
		              name: 'cose',
		              idealEdgeLength: 200,
		              fit: true,
		              
		            },

		            style: finalStyle,

		            elements: finaldata

		          });
		        
		        addTooltipds(cyGeneral);
		        //$("#header h1").text("Visualization of sample result");
		        cyGeneral.ready(function(){
		        	
//		        	var defaults = {
//		        			  zoomFactor: 0.05, // zoom factor per zoom tick
//		        			  zoomDelay: 45, // how many ms between zoom ticks
//		        			  minZoom: 0.1, // min zoom level
//		        			  maxZoom: 10, // max zoom level
//		        			  fitPadding: 50, // padding when fitting
//		        			  panSpeed: 10, // how many ms in between pan ticks
//		        			  panDistance: 10, // max pan distance per tick
//		        			  panDragAreaSize: 75, // the length of the pan drag box in which the vector for panning is calculated (bigger = finer control of pan speed and direction)
//		        			  panMinPercentSpeed: 0.25, // the slowest speed we can pan by (as a percent of panSpeed)
//		        			  panInactiveArea: 8, // radius of inactive area in pan drag box
//		        			  panIndicatorMinOpacity: 0.5, // min opacity of pan indicator (the draggable nib); scales from this to 1.0
//		        			  zoomOnly: false, // a minimal version of the ui only with zooming (useful on systems with bad mousewheel resolution)
//		        			  fitSelector: undefined, // selector of elements to fit
//		        			  animateOnFit: function(){ // whether to animate on fit
//		        			    return false;
//		        			  },
//		        			  fitAnimationDuration: 1000, // duration of animation on fit
//
//		        			  // icon class names
//		        			  sliderHandleIcon: 'fa fa-minus',
//		        			  zoomInIcon: 'fa fa-plus',
//		        			  zoomOutIcon: 'fa fa-minus',
//		        			  resetIcon: 'fa fa-expand'
//		        			};
//
//		        	cyGeneral.panzoom( defaults );
		        	cyGeneral.center();
		        });
		        
		    });

			} catch (err) {
		console.log(err);
			}
}

function addTooltipds(cy){
    cy.edges().forEach(function(ele) {
      ele.qtip({
        content: {
          text: ele.data('tooltip'),
        },
        style: {
          classes: 'qtip-bootstrap'
        },
        position: {
          my: 'bottom center',
          at: 'top center',
          target: ele
        }
      });
    });

    
    cy.nodes().forEach(function(ele) {
        ele.qtip({
          content: {
        	title: (ele.data('tooltip')!=undefined) ? 'sentiment recognition': ele.data('name'),
            text: (ele.data('tooltip')!=undefined) ? ele.data('tooltip') : '',
          },
          style: {
            classes: 'qtip-bootstrap'
          },
          position: {
            my: 'bottom center',
            at: 'top center',
            target: ele
          }
        });
      });

}

function addTemporals(profile){
	const temporals = profile.temporals;
	let tempArr = [];
	const temporalsNode ={
			"data": {
			   	"id": 'temporals' + profile.name,
			    "name": 'temporals',
			    "score": 0,
			    "query": true,
				"gene": true,
			  },
			  "group": "nodes",
			  "classes": 'collection'
	};
	const temporalsEdge={
			"data": {
  			    "source": profile.name,
  			    "target": 'temporals' + profile.name,
  			    "weight": 0.1,
  			    "id": 'edge_temporals'+profile.name
  			  },
  			  "group": "edges",
  			  "classes": ""
	}
	tempArr.push(temporalsNode);
	tempArr.push(temporalsEdge);
	
	temporals.forEach((temp) =>{
		if(availableTags.indexOf('Show Characters with ' + temp) < 0)
			availableTags.push('Show Characters with ' + temp);
		const tempNode ={
				"data": {
    			   	"id": 'temporals' + profile.name + temp,
    			    "name": temp,
    			    "score": 0,
    			    "query": true,
    				"gene": true
    			  },
    			  "group": "nodes",
    			  "classes": "temp"
		}; 
		const tempEdge={
				"data": {
      			    "source": 'temporals' + profile.name,
      			    "target": 'temporals' + profile.name + temp,
      			    "weight": 0.1,
      			    "group": profile.name,
      			    "id": 'temporals' + profile.name + 'temporals' + profile.name + temp
      			  },
      			  "group": "edges",
      			  "classes": ""
		};
		tempArr.push(tempNode);
		tempArr.push(tempEdge);
	});
	
	return tempArr;
}
function addLocations(profile){
	const locations = profile.locations;
	let locArr = [];
	const locationsNode ={
			"data": {
			   	"id": 'locations' + profile.name,
			    "name": 'locations',
			    "score": 0,
			    "query": true,
				"gene": true
			  },
			  "group": "nodes",
			  "classes": 'collection'
	};
	const locationsEdge={
			"data": {
  			    "source": profile.name,
  			    "target": 'locations' + profile.name,
  			    "weight": 0.1,
  			    "id": 'edge_locations'+profile.name
  			  },
  			  "group": "edges",
  			  "classes": ""
	}
	locArr.push(locationsNode);
	locArr.push(locationsEdge);
	
	locations.forEach((loc) =>{
		if(availableTags.indexOf('Show Characters with ' + loc) < 0)
		availableTags.push('Show Characters with ' + loc);
		const locationNode ={
				"data": {
    			   	"id": 'locations' + profile.name + loc,
    			    "name": loc,
    			    "score": 0,
    			    "query": true,
    				"gene": true
    			  },
    			  "group": "nodes",
    			  "classes": ""
		}; 
		const locationEdge={
				"data": {
      			    "source": 'locations' + profile.name,
      			    "target": 'locations' + profile.name + loc,
      			    "weight": 0.1,
      			    "group": profile.name,
      			    "id": 'locations' + profile.name + 'locations' + profile.name + loc
      			  },
      			  "group": "edges",
      			  "classes": ""
		};
		locArr.push(locationNode);
		locArr.push(locationEdge);
	});
	return locArr;
}

function addVerbs(profile){
	const verbs = profile.verbs;
	let verbArr = [];
	const verbsNode ={
			"data": {
			   	"id": 'verbs' + profile.name,
			    "name": 'verbs',
			    "score": 0,
			    "query": true,
				"gene": true
			  },
			  "group": "nodes",
			  "classes": 'collection'
	};
	const verbsEdge={
			"data": {
  			    "source": profile.name,
  			    "target": 'verbs' + profile.name,
  			    "weight": 0.1,
  			    "id": 'edge_verbs'+profile.name
  			  },
  			  "group": "edges",
  			  "classes": ""
	}
	verbArr.push(verbsNode);
	verbArr.push(verbsEdge);
	
	for(verb in verbs)
	{
		if(availableTags.indexOf('Show Characters with ' + verb) < 0)
		availableTags.push('Show Characters with ' + verb);
		const verbCount = verbs[verb];
		const verbNode ={
				"data": {
    			   	"id": 'verbs' + profile.name + verb,
    			    "name": verb,
    			    "score": 0,
    			    "query": true,
    				"gene": true
    			  },
    			  "group": "nodes",
    			  "classes": ""
		}; 
		const verbEdge={
				"data": {
      			    "source": 'verbs' + profile.name,
      			    "target": 'verbs' + profile.name + verb,
      			    "weight": 0.1,
      			    "group": profile.name,
      			    "id": 'verbs' + profile.name + 'verbs' + profile.name + verb
      			  },
      			  "group": "edges",
      			  "classes": ""
		};
		verbArr.push(verbNode);
		verbArr.push(verbEdge);
	};
	return verbArr;
}

function addPersonalities(profile){
	const personalities = profile.personality;
	let persArr = [];
	const personalitiesNode ={
			"data": {
			   	"id": 'personalities' + profile.name,
			    "name": 'personality',
			    "score": 0,
			    "query": true,
				"gene": true
			  },
			  "group": "nodes",
			  "classes": 'collection'
	};
	const personalitiesEdge={
			"data": {
  			    "source": profile.name,
  			    "target": 'personalities' + profile.name,
  			    "weight": 0.1,
  			    "id": 'edge_personalities'+profile.name
  			  },
  			  "group": "edges",
  			  "classes": ""
	}
	persArr.push(personalitiesNode);
	persArr.push(personalitiesEdge);
	
	for(pers in personalities)
	{
		const persAmount = Math.round(personalities[pers]);
		const persNode ={
				"data": {
    			   	"id": 'personalities' + profile.name + pers,
    			    "name": pers + ":" + persAmount,
    			    "score": 0,
    			    "query": true,
    				"gene": true
    			  },
    			  "group": "nodes",
    			  "classes": ""
		}; 
		const persEdge={
				"data": {
      			    "source": 'personalities' + profile.name,
      			    "target": 'personalities' + profile.name + pers,
      			    "weight": 0.1,
      			    "group": profile.name,
      			    "id": 'personalities' + profile.name + 'personalities' + profile.name + pers
      			  },
      			  "group": "edges",
      			  "classes": ""
		};
		persArr.push(persNode);
		persArr.push(persEdge);
	};
	return persArr;	
}

function addAdjc(profile){
	const adjs= profile.adjs;
	let adjArr = [];
	const adjsNode ={
			"data": {
			   	"id": 'adjs' + profile.name,
			    "name": 'adjs',
			    "score": 0,
			    "query": true,
				"gene": true
			  },
			  "group": "nodes",
			  "classes": 'collection'
	};
	const adjsEdge={
			"data": {
  			    "source": profile.name,
  			    "target": 'adjs' + profile.name,
  			    "weight": 0.1,
  			    "id": 'edge_adjs'+profile.name
  			  },
  			  "group": "edges",
  			  "classes": ""
	}
	adjArr.push(adjsNode);
	adjArr.push(adjsEdge);
	
	for(adj in adjs){
		if(availableTags.indexOf('Show Characters with ' + adj) < 0)
		availableTags.push('Show Characters with ' + adj);
		const adjNode ={
				"data": {
    			   	"id": 'adjs' + profile.name + adj,
    			    "name": adj,
    			    "score": 0,
    			    "query": true,
    				"gene": true
    			  },
    			  "group": "nodes",
    			  "classes": ""
		}; 
		const adjEdge={
				"data": {
      			    "source": 'adjs' + profile.name,
      			    "target": 'adjs' + profile.name + adj,
      			    "weight": 0.1,
      			    "group": profile.name,
      			    "id": 'adjs' + profile.name + 'adjs' + profile.name + adj
      			  },
      			  "group": "edges",
      			  "classes": ""
		};
		adjArr.push(adjNode);
		adjArr.push(adjEdge);
	};
	return adjArr;	
}


function renderTimeLineGraph() {	
	let fileName = "";
	const mode = $('#selectMode option:checked').val();
	if(mode === "story"){
		fileName = 'chapters_story.json';
	}else{
		fileName = 'chapters_'+mode+'.json';
	}
	try {
		Promise.all([
		    fetch(fileName, {mode: 'no-cors'})
		    .then(function(res) {
		      return res.json()
		    })]).then(function (dataArray){
		    	let chapters = dataArray[0].chapters;
		    	let profiles = dataArray[0].profileNames; 
		    	let finaldata = [];
		    	let tooltipData = [];
		    	profiles.forEach((profileName)=>{
		    				    	
		    		data = [];
		    		chapters.forEach((chapter)=>{
		    			let adjLength = 0;
		    			let adjNames = [];
		    			for(profile in chapter.profiles){
		    				if(profileName === profile){
		    					const adjs = chapter.profiles[profileName];
		    					for(adj in adjs){
		    						adjLength++;
		    						adjNames.push(adj);
		    					}
		    				}
		    			}
		    			if(adjLength == 0){
		    				data.push(null);
		    			}else{
		    				data.push(adjLength);
		    				tooltipData.push({
		    					'chapter': chapter.chapterIndex,
		    					'profile': profileName,
		    					'tooltip': adjNames,
		    				});
		    			}
		    		});
		    		
		    		const serie ={
		    		        name: profileName,
		    		        data: data
		    		        };
		    		finaldata.push(serie);
		    	});

		    	finaldata.forEach((serie)=>{
		    		let shouldRemove = true;
		    		const data = serie.data;
		    		data.forEach((count)=>{
			    		if(count != null){
			    			shouldRemove = false;
			    		}
			    	});
		    		if(shouldRemove){
		    			const index = finaldata.indexOf(serie);
		    			finaldata.splice(index, 1);
		    		}
		    			
		    	});
		    	
		    	Highcharts.chart('distributionContainer', {
		    	    chart: {
		    	        type: 'column'
		    	    },
		    	    plotOptions: {
		    	        column: {
		    	            
		    	        }
		    	    },
		    	    title: {
		    	        text: 'Distribution of adjectives of character over time/chapter'
		    	    },
		    	    xAxis: {
		    	        categories: ['Chapter1', 'Chapter2', 'Chapter3', 'Chapter4', 'Chapter5','Chapter6', 'Chapter7','Chapter8','Chapter9'],
		    	        title: {
		    	            text: null
		    	        }
		    	    },
		    	    yAxis: {
		    	        title: {
		    	            text: 'number of adjectives',
		    	            align: 'high'
		    	        },
		    	        labels: {
		    	            overflow: 'justify'
		    	        }
		    	    },
		    	    tooltip: {
		    	        formatter: function() {
		    	        	for(t in tooltipData){
		    	        		const tooltip = tooltipData[t];
		    	        		if("Chapter" + tooltip.chapter == this.x && tooltip.profile == this.series.name){
		    	        			tooltipStr = tooltip.tooltip; 
		    	        			return 'The adjectives for <b>' + this.series.name + '</b> are <b>' + tooltipStr + '</b>';		
		    	        		}
		    	        	}
		    	        	tooltipData.forEach((tooltip) =>{
		    	        		return 's';
		    	        		if(tooltip.chapter - 1 == this.x && this.series.name == tooltip.profile){
		    	        			tooltipStr = tooltip.tooltip; 
		    	        			return 'The value for <b>' + this.series.name + '</b> is <b>' + tooltipStr + '</b>';		
		    	        		}
		    	        	});
		    	            
		    	        }
		    	    },
		    	    legend: {
		    	        layout: 'horizontal',
		    	        align: 'center',
		    	        verticalAlign: 'bottom',
		    	        borderWidth: 1,
		    	        backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
		    	        shadow: true
		    	    },
		    	    credits: {
		    	        enabled: false
		    	    },
		    	    series: finaldata
		    	});
		    })} catch (err) {
		
			}
}

function renderGanttGraph() {
	
	let fileName;
	const mode = $('#selectMode option:checked').val();
	if(mode === "story"){
		fileName = 'chapters_story.json';
	}else{
		fileName = 'chapters_'+mode+'.json';
	}
	
	try {
		Promise.all([
		    fetch(fileName, {mode: 'no-cors'})
		    .then(function(res) {
		      return res.json()
		    })]).then(function (dataArray){
		    	let chapters = dataArray[0].chapters;
		    	let profiles = dataArray[0].profileNames; 
		    	let finaldata = [];
		    	let tooltipData = [];
		    	profiles.forEach((profileName)=>{
		    		let firstChapterIndex = 0;
	    			let lastChapterIndex = 0;
		    		chapters.forEach((chapter)=>{
		    			for(profile in chapter.profiles){
		    				if(profileName === profile){
		    					if(firstChapterIndex === 0){
		    						firstChapterIndex = chapter.chapterIndex;
		    						lastChapterIndex = firstChapterIndex;
		    					}
		    					else
		    						lastChapterIndex = chapter.chapterIndex;
		    				}
		    			}
		    			
		    		});
		    		finaldata.push([firstChapterIndex - 0.5,lastChapterIndex + 0.5]);
		    	});
		    	Highcharts.chart('Ganttcontainer', {

		    	    chart: {
		    	        type: 'columnrange',
		    	        inverted: true
		    	    },

		    	    title: {
		    	        text: 'Time line'
		    	    },


		    	    xAxis: {
		    	        categories: profiles
		    	    },

		    	    yAxis: {
		    	        title: {
		    	            text: ''
		    	        },
		    	        categories: ['Initial', 'Chapter1', 'Chapter2', 'Chapter3', 'Chapter4', 'Chapter5', 'Chapter6', 'Chapter7', 'Chapter8', 'Chapter9']
		    	    },


		    	    plotOptions: {
		    	        columnrange: {
		    	            dataLabels: {
		    	                enabled: false
		    	                }
		    	        }
		    	    },

		    	    legend: {
		    	        enabled: false
		    	    },

		    	    series: [{
		    	        name: 'Life',
		    	        data: finaldata
		    	    }]

		    	});

		    })} catch (err) {
		
			}
}

});