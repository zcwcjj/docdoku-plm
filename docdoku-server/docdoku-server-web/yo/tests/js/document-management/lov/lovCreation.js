/*global casper,urls,documents*/

casper.test.begin('LOV creation tests suite',4, function documentLOVCreationTestsSuite(){

    'use strict';

    casper.open('');

    /**
     * Open document management URL
     * */

    casper.then(function(){
        this.open(urls.documentManagement);
    });

    /**
     * Open template nav
     */

    casper.then(function waitForTemplateNavLink(){
        this.waitForSelector('#template-nav > .nav-list-entry > a',function clickTemplateNavLink() {
            this.click('#template-nav > .nav-list-entry > a');
        });
    });

    /**
     * Open LOV creation modal
     */

    casper.then(function waitForLOVCreationLink(){
        this.waitForSelector('.actions .list-lov',function clickOnLOVCreationLink(){
            this.click('.actions .list-lov');
        });
    });

    /**
     * Wait for lov creation modal and click on add
     */

    casper.then(function waitForLOVCreationModal(){
        this.waitForSelector('.list-lov',function lovItemCreationModalDisplayed(){
            this.click('.addLOVButton');
            this.test.assertExists('.lovItem', 'An item should be added to the list');
        });
    });

    /**
     * Try creation of empty item
     */

    casper.then(function tryCreateLOVEmpty(){
        this.click('.btn-saveLovs');
        this.test.assertExists('input.lovItemNameInput:invalid', 'Should not create lov without a name');
    });

    /**
     * Fill item name
     */

    casper.then(function addNameToLOVItem(){
        this.sendKeys('input.lovItemNameInput',documents.lov.itemName);
    });

    /**
     * Try creation of empty possible value
     */

    casper.then(function tryCreateLOVWithEmptyPossibleValue(){
        this.click('.btn-saveLovs');
        this.test.assertExists('input.lovItemNameValueNameInput:invalid', 'Should not create lov without a name');
    });

    /**
     * Fill item possible value
     */

    casper.then(function addLOVItemPossibleValue(){
        this.sendKeys('input.lovItemNameValueNameInput',documents.lov.possibleValueName);
        this.sendKeys('input.lovItemNameValueValueInput',documents.lov.possibleValueValue);
    });

    /**
     * Try creation
     */

    casper.then(function tryCreateLOV(){
        this.click('.btn-saveLovs');
    });

    /**
     * Re-open the modal to check
     */
    casper.then(function reopenModal(){
        this.waitForSelector('.actions .list-lov',function clickOnLOVCreationLink(){
            this.click('.actions .list-lov');
        });
    });

    /**
     * Check the number of item
     */

    casper.then(function waitForLOVCreationModal(){
        this.waitForSelector('.list-lov',function checkLOVPersistence(){
            this.test.assertExists('.lovItem', "One element should be in the list of LOV");
        });
    });

    casper.run(function allDone() {
        this.test.done();
    });
});
