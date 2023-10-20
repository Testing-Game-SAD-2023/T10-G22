function checkboxStateChanged(){
    var checkboxValue = document.getElementById("RangeCheckbox").checked;
    if(checkboxValue){
        var divsection = document.getElementById("home-container");
        var newInputType = document.createElement("input");
        var newSpan = document.createElement("span");
        newInputType.type ="date";
        newInputType.id="date2";
        newInputType.placeholder="placeholder";
        newInputType.classList.add("home-textinput");
        newSpan.textContent="To";
        newSpan.id="SpanTo";
        newSpan.classList.add("home-text1");

        divsection.appendChild(newSpan);
        divsection.appendChild(newInputType);
    }else{
        var newInputType = document.getElementById("date2");
        var textlabel = document.getElementById("SpanTo");
        newInputType.remove();
        textlabel.remove();
    }
}

function checkboxAllStateChanged(){
    var checkboxValue = document.getElementById("AllCheckbox").checked;
    var CheckBoxRange = document.getElementById("RangeCheckbox");
    var RangeDate = document.getElementById("date2");
    var Date = document.getElementById("date");
    var Span = document.getElementById("SpanTo");

    if(checkboxValue){
        CheckBoxRange.disabled = true;
        CheckBoxRange.checked = false;
        Date.disabled = true;
        if( RangeDate != null){
            RangeDate.remove();
        }
        if(Span != null){
            Span.remove();
        }
    }else{
        CheckBoxRange.disabled = false;
        Date.disabled = false;
    }
}
function loadErrors(){
    var checkboxValueAll = document.getElementById("AllCheckbox").checked;
    if(checkboxValueAll){
        makeRequest(0,null,null);
    }else{
        var checkboxValueRange = document.getElementById("RangeCheckbox").checked;
        if(checkboxValueRange){
            var date1 = document.getElementById("date").value;
            var date2 = document.getElementById("date2").value;
            if(isEmpty(date1) || isEmpty(date2) || isLess(date2,date1)){
                alert("Settare delle date valide");
            }else{
                makeRequest(2,date1,date2);
            }
        }else{
            var date = document.getElementById("date").value;
            if(isEmpty(date)){
                alert("Settare una data valida");
            }else{
                makeRequest(1,date,null);
            }
        }
    }
}
function isEmpty(value){
    return value === null || value === undefined || value === '';
}
function isLess(date_1,date_2){ //check if date_1<date_2
    var date1 = new Date(date_1);
    var date2 = new Date(date_2);
    return date1<date2;
}

function makeRequest(requestType,date1,date2){
    var url="http://localhost:8080/api/errorRepo/";
    switch (requestType){
        case 0: //Tutti gli errori
            url = url+"getAllErrors";
            break;
        case 1: //Errori in una determinata data
            url = url+"getErrorsByDate?date="+date1;
            break;
        case 2: //Errori tra due date
            url = url+"errorBetween?date1="+date1+"&date2="+date2;
            break;
        default:
            break;
    }
    fetch(url,{
        method:'GET',
        mode: 'cors',
        headers: {}
    })
        .then(response => {
            if(!response.ok){
                console.log("Errore nella richiesta");
                throw new Error("Request error");
            }
            return response.text();
        })
        .then(data => {
            var errors = ErrorJsonConverter(data.split("),"));
            generateCards(errors);
        })
        .catch(error => {
            console.error(error);
        })
}
function generateCards(errors){
    var divToAppend = document.getElementById("cardContainer");
    while (divToAppend.firstChild) {
        divToAppend.removeChild(divToAppend.firstChild);
    }
    errors.forEach(function(error){
        // Crea gli elementi HTML
        var divHomeFeatureCard = document.createElement("div");
        divHomeFeatureCard.classList.add("home-feature-card");

        var svgElement = document.createElement("svg");
        svgElement.setAttribute("viewBox", "0 0 1024 1024");
        svgElement.classList.add("home-icon");

        var pathElement = document.createElement("path");
        pathElement.setAttribute("d", "M809.003 ..."); // Aggiungi il valore dell'attributo "d" desiderato

        var divHomeContainer4 = document.createElement("div");
        divHomeContainer4.classList.add("home-container4");

        var h2Element = document.createElement("h2");
        h2Element.classList.add("home-text2");
        h2Element.textContent = "Errore " + error['id'];

        var spanElement = document.createElement("span");
        spanElement.innerHTML = "<b>data:"+ error['date'] +"</b><br><b>Status Code:"+error['errorCode']+"</b><br><b>Sorgente: "+error['from_req']+"</b><br><b>Descrizione: "+error['description']+"</b>";

// Aggiungi gli elementi creati al punto desiderato del documento HTML
        divHomeFeatureCard.appendChild(svgElement);
        svgElement.appendChild(pathElement);
        divHomeFeatureCard.appendChild(divHomeContainer4);
        divHomeContainer4.appendChild(h2Element);
        divHomeContainer4.appendChild(spanElement);

        divToAppend.appendChild(divHomeFeatureCard);
    })

}
function ErrorJsonConverter(data){
    var jsonArray = [];
    data.forEach(function (element){
        var validData = element.split("Error(")[1];
        var coppie = validData.split(", ");
        var jsonData = {};
        coppie.forEach(function(coppia){
            var [chiave,valore]=coppia.split('=');
            if(!isEmpty(chiave) || !isEmpty(valore)){
                jsonData[chiave] = valore;
                console.log(chiave+"="+valore);
            }

        });
        jsonArray.push(jsonData);
    })
    jsonArray[jsonArray.length-1]['from_req'] = jsonArray[jsonArray.length-1]['from_req'].split(")]")[0];
    return jsonArray;
}