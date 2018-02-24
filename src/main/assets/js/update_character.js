var _table_ = document.createElement('table'),
    _tr_ = document.createElement('tr'),
    _th_ = document.createElement('th'),
    _td_ = document.createElement('td');

function httpGetAsync()
{
    var input = document.getElementById('input').value;

    console.debug(input);

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() {
        if (xmlHttp.readyState === 4 && xmlHttp.status === 200) {

            var response = JSON.parse(xmlHttp.responseText);
            delete response.stats;
            console.debug(response);
            var stats = JSON.parse(xmlHttp.responseText).stats;
            console.debug(stats);

            if (document.getElementById('table').childElementCount !== 0) {
                document.getElementById('table').replaceChild(buildHtmlTable([
                    response
                ]), document.getElementById('table').childNodes[0]);
            } else {
                document.getElementById('table').appendChild(buildHtmlTable([
                    response
                ]));
            }

            if (document.getElementById('stats').childElementCount !== 0) {
                document.getElementById('stats').replaceChild(buildHtmlTable([
                    stats
                ]), document.getElementById('stats').childNodes[0]);
            } else {
                document.getElementById('stats').appendChild(buildHtmlTable([
                    stats
                ]));
            }
        }
    };

    xmlHttp.open("GET", config["api-base-url"] + "/character/" + input, true); // true for asynchronous
    xmlHttp.send(null);
}

function buildHtmlTable(arr) {
    var table = _table_.cloneNode(false),
        columns = addAllColumnHeaders(arr, table);
    for (var i=0, maxi=arr.length; i < maxi; ++i) {
        var tr = _tr_.cloneNode(false);
        for (var j=0, maxj=columns.length; j < maxj ; ++j) {
            var td = _td_.cloneNode(false);
            cellValue = arr[i][columns[j]];
            td.appendChild(document.createTextNode(arr[i][columns[j]] || ''));
            tr.appendChild(td);
        }
        table.appendChild(tr);
    }
    return table;
}

function addAllColumnHeaders(arr, table)
{
    var columnSet = [],
        tr = _tr_.cloneNode(false);
    for (var i=0, l=arr.length; i < l; i++) {
        for (var key in arr[i]) {
            if (arr[i].hasOwnProperty(key) && columnSet.indexOf(key)===-1) {
                columnSet.push(key);
                var th = _th_.cloneNode(false);
                th.appendChild(document.createTextNode(key));
                tr.appendChild(th);
            }
        }
    }
    table.appendChild(tr);
    return columnSet;
}
