### Example of running Serverless with Code Engine on IBM Cloud ###


This is a small example of using [Code Engine](https://www.ibm.com/cloud/code-engine) to create a sample flow with different small applications that utilise IBM MQ, Cloudant DB and is writen in Java, PHP, though Cloud Engine supports several other languages.  This is a similar example to the [Serverless on IBM Cloud](https://github.com/bpaskin/Serverless-on-IBM-Cloud) which utilises [IBM Cloud Functions](https://cloud.ibm.com/functions/) for Serverless computing.

![Diagram of flow](https://github.com/bpaskin/IBM-Cloud-Functions/blob/main/images/serverless.drawio.png?raw=true)

---
#### Prerequistes:
- An [IBM Cloud Account](https://cloud.ibm.com)
- The [IBM Cloud CLI](https://cloud.ibm.com/docs/cli?topic=cli-getting-started)
- A separate instance of [IBM MQ](https://www.ibm.com/products/mq) to allow for triggering and is available on the internet for requests.  A [containerized IBM MQ](https://hub.docker.com/r/ibmcom/mq/) can be used, as well.
- A Docker type of repository to store OCI images.  [quay.io](quay.io) allows for free public images.

---
#### Setup Cloudant
All the steps can be done through the console.  Some, but not all, can be done using the CLI

Create an [instance](https://cloud.ibm.com/docs/Cloudant?topic=Cloudant-creating-an-ibm-cloudant-instance-on-ibm-cloud-by-using-the-ibm-cloud-cli
):
- login to the IBM Cloud and select which account to use </br>
`ibmcloud login`
- Target the resource </br>
`ibmcloud target -g default`
- Create an instance called </br>
`cloudant-serverless` (`ibmcloud resource service-instance-create cloudant-serverless cloudantnosqldb lite us-south -p '{"legacyCredentials": false}'`
- Generate the credentials </br>
`ibmcloud resource service-key-create serverless-creds Manager --instance-name cloudant-serverless`
- Show the credentials </br>
`ibmcloud resource service-key serverless-creds`

The next steps need to be done in the Console UI.
- Select the `cloudant-serverless` instance of the DB
- Click on `Launch Dashboard` blue button in the upper right.  This will open the Cloudant instance in a new panel.
- Click on `Create Database` in the upper right, which will then review a panel.
- Enter the name of the database as `eurovision` and click `Create`

---
#### Take note of the folowing information that will be used for environmental variables for some programs

- IAM Key for Cloudant (Unique for each instance)
- URL For Cloudant (Unique for each instance)
- Service Name for Cloudant (cloudant-serverless)
- Database Name for Cloudant (eurovision)
- MQ QMGR Name (SERVERLESS)
- MQ Channel Name (SERVERLESS.SVRCONN)
- MQ Queue (EUROVISIONCE)
- MQ Host (unqiue)
- MQ Port (9414)

---
#### Code Engine Setup

- Create a project</br>
`ibmcloud ce project create -n EurovisionCE`
- Setup repsitory secrets</br>
`ibmcloud ce registry create -n <registry_name> -e <email_address> -u <userid> -p <password> -s <registry_url>`
Example </br>
`ibmcloud ce registry create -n quay.io -e email@email.com -u myId -p myPassw0rd -s quay.io`</br>
</br>

- Create the definitions for a build for the Voting web page (uses a Dockerfile) </br>
`ibmcloud ce build create --name voteui --source https://github.com/bpaskin/Cloud-Engine-Serverless.git --context-dir voteui --strategy dockerfile --image <registry_url>/voteui --registry-secret quay.io`
- Build an image and store it in the registry</br>
`ibmcloud ce buildrun submit --build voteui`
- Check on the build run using the name from previous command (ie. Submitting build run 'voteui-run-220709-165101135')</br>
`ibmcloud ce buildrun logs -n <name from above command>`
- Create a new application</br>
`ibmcloud ce app create --name voteui --image <registry_url>/voteui --registry-secret quay.io --visibility public --max-scale 4 --port 80`
The Endpont will be displayed after deployment.</br>
</br>

- Create the definition for a build for accepting the vote and sending it to MQ</br>
`ibmcloud ce build create --name acceptvote --source https://github.com/bpaskin/Cloud-Engine-Serverless.git --context-dir acceptVote --strategy buildpacks --image <registry_url>/acceptvote --registry-secret quay.io`
- Build an image and store it in the registry</br>
`ibmcloud ce buildrun submit --build acceptvote`
- Check on the build run using the name from previous command (ie. Submitting build run 'voteui-run-220709-165101135')</br>
`ibmcloud ce buildrun logs -n <name from above command>`
- Create a new application</br>
`ibmcloud ce app create --name acceptvote --image <registry_url>/acceptvote --registry-secret quay.io --visibility public --max-scale 4 -e MQ_QMGR=SERVERLESS -e MQ_HOST=<mq_host_name)  -e MQ_PORT=9414 -e MQ_QUEUE=EUROVISIONCE -e MQ_CHANNEL=SERVERLESS.SVRCONN`
The Endpont will be displayed after deployment.</br>
</br>

- Create the definition for a build for the Results web page</br>
`ibmcloud ce build create --name resultsui --source https://github.com/bpaskin/Cloud-Engine-Serverless.git --context-dir resultsui --strategy buildpacks --image <registry_url>/resultsui --registry-secret quay.io`
- Build an image and store it in the registry</br>
`ibmcloud ce buildrun submit --build resultsui`
- Check on the build run using the name from previous command</br>
`ibmcloud ce buildrun logs -n <name from above command>`
- Create a new application</br>
`ibmcloud ce app create --name resultsui --image <registry_url>/resultsui --registry-secret quay.io --visibility public --max-scale 6`
The Endpont will be displayed after deployment.</br>
</br>

- Create the definitions for the a build for the query of the database</br>
`ibmcloud ce build create --name resultsquery --source https://github.com/bpaskin/Cloud-Engine-Serverless.git --context-dir resultsQuery --strategy buildpacks --image <registry_url>/resultsquery --registry-secret quay.io`
- Build the image based on a buildpack and store it in the registry</br>
`ibmcloud ce buildrun submit --build resultsquery`
- Check on the build run using the name from previous command</br>
`ibmcloud ce buildrun logs -n <name from above command>`
- Create a new application</br>
`ibmcloud ce app create --name resultsquery --image <registry_url>/resultsquery --registry-secret quay.io --visibility public --max-scale 4 -e IAMKEY=<your IAM key> -e SERVICE_NAME=cloudant-serverless -e SERVICE_URL=<your cloudant url> -e DBNAME=eurovision`
The Endpont will be displayed after deployment.</br>
</br>

- Create the definitions for the a build to write a record (document) to Cloudant DB</br>
`ibmcloud ce build create --name writerecord --source https://github.com/bpaskin/Cloud-Engine-Serverless.git --context-dir writeToDB --strategy buildpacks --image <repository_url>/writerecord --registry-secret quay.i `
- Build the image based on a buildpack and store it in the registry</br>
`ibmcloud ce buildrun submit --build writerecord`
- Check on the build run using the name from previous command</br>
`ibmcloud ce buildrun logs -n <name from above command>`
- Create a new application</br>
`ibmcloud ce app create --name writerecord --image <repsitory_url>/writerecord --registry-secret quay.io --visibility private -e IAMKEY=<cloudant_iamkey> -e SERVICE_NAME=cloudant-serverless -e SERVICE_URL=<cloudant service url> -e DBNAME=eurovision`
The Endpont will be displayed after deployment.</br>
</br>

- Create the definition for a build for receiving a message from MQ</br>
`ibmcloud ce build create --name getmessages --source https://github.com/bpaskin/Cloud-Engine-Serverless.git --context-dir retrieveFromMQ --strategy buildpacks --image <registry_url>/getmessages --registry-secret quay.io`
- Build the image based on a buildpack and store it in the registry</br>
`ibmcloud ce buildrun submit --build getmessages`
- Check on the build run using the name from previous command</br>
`ibmcloud ce buildrun logs -n <name from above command>`
- Create a new application</br>
`ibmcloud ce app create --name getmessages --image <registry_url>/getmessages --registry-secret quay.io --visibility public --max-scale 4 -e MQ_QMGR=SERVERLESS -e MQ_HOST=<mq_hostname> -e MQ_PORT=9414 -e MQ_QUEUE=EUROVISIONCE -e MQ_CHANNEL=SERVERLESS.SVRCONN -e DB_URL=<writerecord_url>`
The Endpont will be displayed after deployment.</br>
</br>

---
#### IBM MQ Setup
This will setup a new QMGR that will accept messages for voting and then trigger and call the `Eurovision/updatingDB` endpoint to process messages

The `EUROVISIONCE.TRIGGER` in the `serverlessce.mqsc` file will need to be updated with the proper URL from the `Eurovision/updatingDB` Action.

- Create the QMGR: </br> 
`/usr/mqm/bin/crtmqm SERVERLESS`
- Start the QMGR: </br>
`/usr/mqm/bin/strmqm SERVERLESS`
- Add the Objects to the QMGR: </br>
`/usr/mqm/bin/runmqsc SERVERLESS < serverlessce.mqsc`
- Shutdown the QMGR: </br>
`/usr/mqm/bin/endmqm -i SERVERLESS`
- Restart the QMGR: </br>
`/usr/mqm/bin/strmqm SERVERLESS`

---
#### Test and Enjoy

-+Funeral Winter+-
