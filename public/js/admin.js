/**
 * The primary Shrty management class.
 */
var instance = null;

var Shrty = function(baseUrl, sessionKey) {
	// set the global singleton
	instance = this;
	
	this.baseUrl = baseUrl;
	this.sessionKey = sessionKey;
	this.pageNavCreated = false;
	// 1-based page numbering
	this.curPage = 1;
	// get the page size
	this.getPageSize();
	// wait this many milliseconds after a change of filter text before firing
	// off a query to filter the links
	this.filterDelay = 500;
	this.filterTimer = null;
	this.filterQuery = "";
	this.sortBy = "title";
	this.sortDir = "asc";
	
	$(document).ready(function() {
		$('#linksPerPageInput').change(function() {
			instance.getPageSize();
			instance.curPage = 1;
			// update the paging
			instance.fetchUrls();
		});
		$('#addUrlBtn').click(function() {
			$('#titleInput').val('');
			$('#urlInput').val('');
			$('#shortCodeInput').val('');
			$('#addUrlModal').modal('show');
		});
		$('#searchInput').on('input', function() {
			// cancel the search timer if one's running
			if (instance.filterTimer != null) {
				clearTimeout(instance.filterTimer);
			}
			// fetch URLs after the specified timeout
			instance.filterTimer = setTimeout(function() {
				instance.filterQuery = $('#searchInput').val();
				// update the URL listing
				instance.fetchUrls();
				// clear the timeout
				instance.filterTimer = null;
			}, instance.filterDelay);
		});
		$('#addUrlSaveBtn').click(function() {
			if (!$('#titleInput').val()) {
				window.alert('Please enter a valid title for the link');
			} else if (!$('#urlInput').val()) {
				window.alert('Please enter a valid URL for the link');
			} else {
				instance.addUrl($('#titleInput').val(), $('#urlInput').val(), $('#shortCodeInput').val() ? $('#shortCodeInput').val() : null,
						function(data) {
							console.log(JSON.stringify(data));
							// update the URLs
							instance.fetchUrls();
							// close the modal window
							$('#addUrlModal').modal('hide');
						});
			}
		});
		// fetch the first page of URLs
		instance.fetchUrls();
	});
}

$.extend(Shrty.prototype, {
	/**
	 * Sets up the page numbering for the given number of total pages.
	 * @param totalPages The total number of pages for the page numbering element.
	 */
	initPageNumbering: function(totalPages) {
		// quick range check
		if (totalPages < 1) {
			totalPages = 1;
		}
		
		// range check
		if (instance.curPage > totalPages) {
			instance.curPage = totalPages;
		}
		
		if (!instance.pageNavCreated) {
			console.log('Creating new page nav');
			$('#pageNav').bootstrapPaginator({
				bootstrapMajorVersion: 3,
				currentPage: instance.curPage,
				totalPages: totalPages,
				numberOfPages: 10,
				shouldShowPage: function(type, page, current) {
					return true;
				},
				onPageClicked: function(e, originalEvent, type, page) {
					// switch pages
					instance.curPage = page;
					console.log("Page "+instance.curPage+" selected");
					// load the page
					instance.fetchUrls();
				}
			});
			instance.pageNavCreated = true;
		} else {
			console.log('Updating existing page nav (cur page '+instance.curPage+', total pages '+totalPages+')');
			$('#pageNav').bootstrapPaginator({
				currentPage: instance.curPage,
				totalPages: totalPages
			});
		}
	},
	
	/**
	 * Helper function to get the page size from the linksPerPageInput select element.
	 */
	getPageSize: function() {
		instance.pageSize = parseInt($('#linksPerPageInput').val());
		console.log('Current page size: '+instance.pageSize);
		return instance.pageSize;
	},
	
	/**
	 * Helper function for generic API request handling functionality.
	 * @param path The relative path for the API function we want to call.
	 * @param method The HTTP method to use.
	 * @param data The (optional) data object to send in the request.
	 * @param success The function to call if successful (failure will be handled automatically).
	 */
	apiRequest: function(path, method, data, success) {
		var opts = {
			url: instance.baseUrl+'api/'+path,
			'method': method,
			accepts: 'application/json',
			contentType: 'application/json',
			dataType: 'json',
			headers: {'X-Session-ID': instance.sessionKey},
			'success': success
		};
		if (data) {
			$.extend(opts, {'data': JSON.stringify(data)})
		}
		$.ajax(opts);
	},
	
	/**
	 * Fetches URLs for the current page.
	 */
	fetchUrls: function() {
		var filterQuery = (instance.filterQuery) ? encodeURIComponent(instance.filterQuery) : "";
		
		console.log('Firing off URL fetch with query: '+filterQuery);
		
		instance.apiRequest('shorturl?query='+filterQuery+'&page='+(instance.curPage-1)+'&pageSize='+instance.pageSize+'&sortBy='+instance.sortBy+'&sortDir='+instance.sortDir,
				'GET', null, function(data) {
			
			console.log(JSON.stringify(data, null, 2));
			
			// first clear the table of all of its entries
			instance.clearUrlTable();
			// initialise the page numbers
			instance.initPageNumbering(Math.ceil(data.total / data.pageSize));
			
			var url, urlId, createdBy, shortUrl;
			
			// populate the table with unique entries
			for (var i=0;i<data.urls.length;i++) {
				url = data.urls[i];
				urlId = 'url'+url.id;
				createdBy = url.createdBy.firstName + ' ' + url.createdBy.lastName;
				$('#linkTable').append('<tr class="url" id="'+urlId+'">'+
						'<td class="url-title"></td>'+
						'<td class="url-shortcode"></td>'+
						'<td class="url-url"></td>'+
						'<td class="url-created"><a href="#"></a></td>'+
						'<td class="url-created-by"></td>'+
						'<td class="url-hit-count"></td>'+
						'<td class="url-delete"></td>'+
						'</tr>');
				$('#'+urlId+' .url-title').text(url.title);
				shortUrl = instance.baseUrl+url.shortCode;
				$('#'+urlId+' .url-shortcode').html('<a href="'+shortUrl+'"></a>');
				$('#'+urlId+' .url-shortcode a').text(url.shortCode);
				$('#'+urlId+' .url-url').html('<a href="'+url.url+'" target="_blank"></a>');
				$('#'+urlId+' .url-url a').text(url.url);
				$('#'+urlId+' .url-created').text(url.created);
				$('#'+urlId+' .url-created-by').html('<a href="mailto:'+url.createdBy.email+'"></a>');
				$('#'+urlId+' .url-created-by a').text(createdBy);
				$('#'+urlId+' .url-hit-count').text(url.hitCount);
				$('#'+urlId+' .url-delete').html('<button type="button" title="Delete" id="deleteUrl'+url.id+'Btn" data-shortcode=""><span aria-hidden="true">&times;</span></button>');
				$('#deleteUrl'+url.id+'Btn').attr('data-shortcode', url.shortCode);
				$('#deleteUrl'+url.id+'Btn').click(function(e) {
					var btn = $(e.target).parent();
					if (window.confirm("Are you sure you want to delete the URL with short code \""+$(btn).attr('data-shortcode')+"\"?")) {
						// delete the link with the given short code
						instance.deleteUrl($(btn).attr('data-shortcode'), instance.fetchUrls);
					}
				});
			}
		});
	},
	
	/**
	 * Function to add a new short URL to the database.
	 * @param title The title of the URL to add.
	 * @param url The destination URL for the new short URL.
	 * @param shortCode An optional custom short code (leave null if the system must generate it).
	 * @param success The function to call on success.
	 */
	addUrl: function(title, url, shortCode, success) {
		instance.apiRequest('shorturl', 'POST', {
			'title': title,
			'url': url,
			'shortCode': shortCode
		}, success);
	},
	
	/**
	 * Deletes the URL with the specified short code.
	 * @param shortCode The short code for which to search.
	 */
	deleteUrl: function(shortCode, success) {
		instance.apiRequest('shorturl/'+shortCode, 'DELETE', null, success);
	},
	
	/**
	 * Clears all of the URLs from the URL table.
	 */
	clearUrlTable: function() {
		$('#linkTable tr.url').remove();
	}
	
});