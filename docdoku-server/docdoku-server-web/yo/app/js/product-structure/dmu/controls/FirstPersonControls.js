/**
 * @author mrdoob / http://mrdoob.com/
 * @author alteredq / http://alteredqualia.com/
 * @author paulirish / http://paulirish.com/
 */

THREE.FirstPersonControls = function ( object, domElement ) {

	this.object = object;
	this.target = new THREE.Vector3( 0, 0, 0 );

	this.domElement = ( domElement !== undefined ) ? domElement : document;

	this.movementSpeed = 1.0;
	this.lookSpeed = 0.005;

	this.lookVertical = true;
	this.autoForward = false;
	// this.invertVertical = false;

	this.activeLook = true;

	this.heightSpeed = false;
	this.heightCoef = 1.0;
	this.heightMin = 0.0;
	this.heightMax = 1.0;

	this.constrainVertical = false;
	this.verticalMin = 0;
	this.verticalMax = Math.PI;

	this.autoSpeedFactor = 0.0;

	this.mouseX = 0;
	this.mouseY = 0;

	this.lat = 0;
	this.lon = 0;
	this.phi = 0;
	this.theta = 0;

	this.moveForward = false;
	this.moveBackward = false;
	this.moveLeft = false;
	this.moveRight = false;
	this.freeze = false;

	this.mouseDragOn = false;

	this.viewHalfX = 0;
	this.viewHalfY = 0;

    this.enabled = true;

	if ( this.domElement !== document ) {

		this.domElement.setAttribute( 'tabindex', -1 );

	}

    this.oculusControls = new THREE.OculusControls( object );

	//
	this.handleResize = function () {

		if ( this.domElement === document ) {

			this.viewHalfX = window.innerWidth / 2;
			this.viewHalfY = window.innerHeight / 2;

		} else {

			this.viewHalfX = this.domElement.offsetWidth / 2;
			this.viewHalfY = this.domElement.offsetHeight / 2;

		}

	};

	this.onMouseDown = function ( event ) {

		if ( this.domElement !== document ) {

			this.domElement.focus();

		}

		event.preventDefault();
		event.stopPropagation();

		if ( this.activeLook ) {

			switch ( event.button ) {

				case 0: this.moveForward = true; break;
				case 2: this.moveBackward = true; break;

			}

		}

		this.mouseDragOn = true;

	};

	this.onMouseUp = function ( event ) {

		event.preventDefault();
		event.stopPropagation();

		if ( this.activeLook ) {

			switch ( event.button ) {

				case 0: this.moveForward = false; break;
				case 2: this.moveBackward = false; break;

			}

		}

		this.mouseDragOn = false;

	};

	this.onMouseMove = function ( event ) {

		if ( this.domElement === document ) {

			this.mouseX = event.pageX - this.viewHalfX;
			this.mouseY = event.pageY - this.viewHalfY;

		} else {

			this.mouseX = event.pageX - this.domElement.offsetLeft - this.viewHalfX;
			this.mouseY = event.pageY - this.domElement.offsetTop - this.viewHalfY;

		}

	};

    this.onKey = function(code, state){

        switch ( code ) {

            case 38: /*up*/
            case 90: /*Z*/ this.moveForward = state; break;

            case 37: /*left*/
            case 81: /*Q*/ this.moveLeft = state; break;

            case 40: /*down*/
            case 83: /*S*/ this.moveBackward = state; break;

            case 39: /*right*/
            case 68: /*D*/ this.moveRight = state; break;

            case 32: /*SPACE*/ this.moveUp = state; break;
            case 17: /*CTRL*/ this.moveDown = state; break;

            // case 65: /*A*/ this.freeze = !this.freeze; break;

        }
    };

	this.onKeyDown = function ( event ) {
        this.onKey(event.keyCode,true);
	};

	this.onKeyUp = function ( event ) {
        this.onKey(event.keyCode,false);
	};

	this.update = function( delta ) {

		if ( this.freeze ) {

			return;

		}

		if ( this.heightSpeed ) {

			var y = THREE.Math.clamp( this.object.position.y, this.heightMin, this.heightMax );
			var heightDelta = y - this.heightMin;

			this.autoSpeedFactor = delta * ( heightDelta * this.heightCoef );

		} else {

			this.autoSpeedFactor = 0.0;

		}

		var actualMoveSpeed = delta * this.movementSpeed;

		if ( this.moveForward || ( this.autoForward && !this.moveBackward ) ) this.object.translateZ( - ( actualMoveSpeed + this.autoSpeedFactor ) );
		if ( this.moveBackward ) this.object.translateZ( actualMoveSpeed );

		if ( this.moveLeft ) this.object.translateX( - actualMoveSpeed );
		if ( this.moveRight ) this.object.translateX( actualMoveSpeed );

		if ( this.moveUp ) this.object.translateY( actualMoveSpeed );
		if ( this.moveDown ) this.object.translateY( - actualMoveSpeed );

		var actualLookSpeed = delta * this.lookSpeed;

		if ( !this.activeLook ) {

			actualLookSpeed = 0;

		}

		var verticalLookRatio = 1;

		if ( this.constrainVertical ) {

			verticalLookRatio = Math.PI / ( this.verticalMax - this.verticalMin );

		}

		this.lon += this.mouseX * actualLookSpeed;
		if( this.lookVertical ) this.lat -= this.mouseY * actualLookSpeed * verticalLookRatio;

		this.lat = Math.max( - 85, Math.min( 85, this.lat ) );
		this.phi = THREE.Math.degToRad( 90 - this.lat );

		this.theta = THREE.Math.degToRad( this.lon );

		if ( this.constrainVertical ) {

			this.phi = THREE.Math.mapLinear( this.phi, 0, Math.PI, this.verticalMin, this.verticalMax );

		}

		var targetPosition = this.target,
			position = this.object.position;

		targetPosition.x = position.x + 100 * Math.sin( this.phi ) * Math.cos( this.theta );
		targetPosition.y = position.y + 100 * Math.cos( this.phi );
		targetPosition.z = position.z + 100 * Math.sin( this.phi ) * Math.sin( this.theta );

		this.object.lookAt( targetPosition );

        this.oculusControls.update( delta );

        this.dispatchEvent({ type: 'change' });
	};

    this.unbindEvents = function () {
        this.domElement.removeEventListener( 'contextmenu', function ( event ) { event.preventDefault(); }, false );
        this.domElement.removeEventListener( 'mousemove', bind( this, this.onMouseMove ), false );
        this.domElement.removeEventListener( 'mousedown', bind( this, this.onMouseDown ), false );
        this.domElement.removeEventListener( 'mouseup', bind( this, this.onMouseUp ), false );
        window.removeEventListener( 'keydown', bind( this, this.onKeyDown ), false );
        window.removeEventListener( 'keyup', bind( this, this.onKeyUp ), false );
    };

    this.bindEvents = function () {
        this.domElement.addEventListener( 'contextmenu', function ( event ) { event.preventDefault(); }, false );
        this.domElement.addEventListener( 'mousemove', bind( this, this.onMouseMove ), false );
        this.domElement.addEventListener( 'mousedown', bind( this, this.onMouseDown ), false );
        this.domElement.addEventListener( 'mouseup', bind( this, this.onMouseUp ), false );
        window.addEventListener( 'keydown', bind( this, this.onKeyDown ), false );
        window.addEventListener( 'keyup', bind( this, this.onKeyUp ), false );
    };

	function bind( scope, fn ) {

		return function () {

			fn.apply( scope, arguments );

		};

	};

    this.getObject = function () {
        return this.object;
    };

    this.getCamUp = function () {
        return this.object.up;
    };

    this.getTarget = function () {
        return this.getDirection(this.target).multiplyScalar(2000).add(object.position);

        //return this.target;
    };

    this.getCamPos = function () {
        return this.object.position.clone();
    };

    this.setCamUp = function (camUp) {
        this.object.up.copy(camUp);
    };

    this.setCamPos = function (camPos) {
        this.object.position.copy(camUp);
    };

    this.setTarget = function (target) {
        this.target.copy(target);
    };

    this.getDirection = function () {

        // assumes the camera itself is not rotated

        var direction = new THREE.Vector3(0, 0, -1);
        var rotation = new THREE.Euler(0, 0, 0, 'YXZ');

        return function (v) {

            rotation.set(object.rotation.x, object.rotation.y, 0);

            v.copy(direction).applyEuler(rotation);

            return v;

        };

    }();

};

THREE.FirstPersonControls.prototype = Object.create(THREE.EventDispatcher.prototype);
