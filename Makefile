.DEFAULT_GOAL := all

all:
	@echo "Please check Makefile"
flink-up:
	kubectl create -f minikube/deployment/jobmanager.yaml
	kubectl create -f minikube/service/jobmanager.yaml
	kubectl create -f minikube/deployment/taskmanager.yaml
flink-delete:
	kubectl delete -f minikube/deployment/jobmanager.yaml
	kubectl delete -f minikube/service/jobmanager.yaml
	kubectl delete -f minikube/deployment/taskmanager.yaml
