# Setting up Attack Vector on AWS ECS

## Prerequisites

- AWS Admin account
- MongoDB Atlas serverless database

## Cost

Running this setup will create services in AWS with a daily cost.

With a minimal container (0.25 vCPU, 1 GB) current pricing (Augustus 2023) this will be around 21 euro per month.

- Half of that price is for the container
- Half of that price is for a public IP address and a VPC endpoint to link to Atlas
- The cost of network, log storage, container storage and database are negligible.

Running with more CPU is recommended for an actual larp, 0.25 vCPU will not give a very responsive experience, but it will work.

If you want to stop the setup from costing money:

- Delete the ECS service
  - This will get rid of the cost of the container and the public IP (13 euro/month current pricing)
- Delete the VPC endpoint
  - This will get rid of the cost of the VPC endpoint (8 euro/month current pricing)

## Steps

1. Create public VPC
1. Allow inbound traffic to public VPC
1. Create a VPC endpoint in Mongodb Atlas
1. Create ECR repository
1. Create Cloudwatch log group
1. Create ECS cluster
1. Create ECS task definition
1. Create ECS service

## Create public VPC

Go to the VPC service in AWS. Create a new VPC using the wizard.

- Select the option to add a public network (2 subnets)

Select VPC

- Remove 1 of the subnets, you only need one.

## Allow inbound traffic to public VPC

Go to the VPC service in AWS. Go to the Security groups section.

- Open the security group for your newly created VPC
- Edit inbound rules
- Add a rule:
  - Type: HTTPt
  - Port range: 80
  - Source: Anywhere-IPv4
  - Description: allow inbound HTTP traffic

## Create a VPC endpoint in Mongodb Atlas

Log in to Mongodb Atlas. Go to the Network Access section, PrivateEndpoint tab : Serverless

Create a new endpoint

- Follow the instructions, filling in the VPC id and subnet id that you just created
  - MongoDB atlas will provide the AWS CLI command to create the endpoint
- It will take a minute or so for Atlas to accept the connection.

## Create ECR repository

Go to the ECR service in AWS. Create a new private repository.

## Push your docker to ECR

See backend/doc/Docker.txt for details on this.

## Create Cloudwatch log group

Go to the Cloudwatch service in AWS.

- Create a new log group with a name like /ecs/<service-name>
- Choose the retention period that you want

## Create ECS cluster

Go to the ECS service in AWS. Create a new cluster

- Select your public VPC and public subnet.

## Create ECS task definition

Go to the ECS service in AWS. Create a new task definition

- Select Fargate
- Operating system: Linux/X86_64
- Task size CPU: choose how fast you want your container to run. 1 vCPU is decent.
- Task size Memory: 1GB is enough.
- Container: Environment variables:
  - set: MONGODB_URI
  - set: MONGODB_NAME
  - set: ENVIRONMENT
- Logging: enable logging to Cloudwatch
  - set the awslogs-stream-prefix to the name of the service (the default ecs is not very useful)
- Leave the Resource limits as they are

## Create ECS service

Go to the cluster and create the service

- Compute options: Launch type
  - Launch type: FARGATE
- Deployment configuration: Service
- Networking:
  - Public IP: Turned on
  - It should already have selected your VPC and public subnet by default

## Access your service

The best place to view the startup of the application is in Cloudwatch.
There will be a separate log stream for each deployment, and you can enable Auto Retry
to follow along as the logs come in.

When the application has started up successfully, go to the service in ECS and to the associated task.
The task will have a public IP address.


