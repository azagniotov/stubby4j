### 1.0.59

* stubby's admin page was not able to display the contents of stubbed response/request ```body```, ```post``` or ```file``` [BUG]
* stubby was not able to match URL when query string param was an array with quoted elements, ie: ```attributes=["id","uuid","created","lastUpdated","displayName","email"]``` [BUG]

### 1.0.58

* Making sure that stubby can serve binary files as well as ascii files, when response is loaded using the ```file``` property [ENHANCEMENT]

### 1.0.57

* Migrated the project from Maven to Gradle (thanks to [Logan McGrath](https://github.com/lmcgrath) for his feedback and assistance). The project has now a multi-module setup [ENHANCEMENT]

### 1.0.56

* If `request.post` was left out of the configuration, stubby would ONLY match requests without a post body to it [BUG]
* Fixing `See Also` section of readme [COSMETICS]

### 1.0.55

* Updated YAML example documentation [COSMETICS]
* Bug fix where command line options `mute`, `debug` and `watch` were overlooked [BUG]

### 1.0.54

* Previous commit (`v1.0.53`) unintentionally broke use of embedded stubby [BUG]


