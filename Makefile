.DEFAULT_GOAL := all

all:
	@echo "Please check Makefile"
flink-up:
	kubectl create -f minikube/config/flink-configuration-configmap.yaml
	kubectl create -f minikube/service/jobmanager.yaml
	kubectl create -f minikube/deployment/jobmanager.yaml
	kubectl create -f minikube/deployment/taskmanager.yaml
flink-delete:
	kubectl delete -f minikube/config/flink-configuration-configmap.yaml
	kubectl delete -f minikube/service/jobmanager.yaml
	kubectl delete -f minikube/deployment/jobmanager.yaml
	kubectl delete -f minikube/deployment/taskmanager.yaml

flink-update:
	kubectl apply -f minikube/config/flink-configuration-configmap.yaml
	kubectl apply -f minikube/service/jobmanager.yaml
	kubectl apply -f minikube/deployment/jobmanager.yaml
	kubectl apply -f minikube/deployment/taskmanager.yaml
