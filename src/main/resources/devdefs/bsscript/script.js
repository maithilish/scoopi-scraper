var execute = function(data){
    
    var items = data.getItems()
    
    for(i=0; i < items.size(); i++){
    	var item = items.get(i)
    	var dateAxis = item.getAxisByItemName("year")
    	var date = dateAxis.getValue()
        var x = moment(date, "MMM 'YY")
        var e = moment(x).endOf('month')
        var d = moment(e).format('DD-MM-YYYY')
        dateAxis.setValue(d)
        
        print(item)	
    }

	
	return data
}
