.DEFAULT_GOAL := all

all:
	@echo "Please check Makefile"
flink-up:
	kubectl create --save-config -f minikube/persistent-volume-claim/flink.yaml
	kubectl create --save-config -f minikube/persistent-volume/flink.yaml
	kubectl create --save-config -f minikube/config/flink-configuration-configmap.yaml
	kubectl create --save-config -f minikube/service/jobmanager.yaml
	kubectl create --save-config -f minikube/deployment/jobmanager.yaml
	kubectl create --save-config -f minikube/deployment/taskmanager.yaml
flink-delete:
	kubectl delete -f minikube/service/jobmanager.yaml
	kubectl delete -f minikube/deployment/jobmanager.yaml
	kubectl delete -f minikube/deployment/taskmanager.yaml

flink-update:
	kubectl apply -f minikube/config/flink-configuration-configmap.yaml
	kubectl apply -f minikube/service/jobmanager.yaml
	kubectl apply -f minikube/deployment/jobmanager.yaml
	kubectl apply -f minikube/deployment/taskmanager.yaml

minio-up:
	kubectl create -f minikube/persistent-volume-claim/minio.yaml
	kubectl create -f minikube/persistent-volume/minio.yaml
	kubectl create -f minikube/service/minio.yaml
	kubectl create -f minikube/deployment/minio.yaml

minio-update:
	kubectl apply -f minikube/service/minio.yaml
	kubectl apply -f minikube/deployment/minio.yaml

minio-delete:
	kubectl delete -f minikube/service/minio.yaml
	kubectl delete -f minikube/deployment/minio.yaml

nessie-up:
	kubectl create -f minikube/persistent-volume-claim/nessie.yaml
	kubectl create -f minikube/persistent-volume/nessie.yaml
	kubectl create -f minikube/service/nessie.yaml
	kubectl create -f minikube/deployment/nessie.yaml

nessie-update:
	kubectl apply -f minikube/service/nessie.yaml
	kubectl apply -f minikube/deployment/nessie.yaml

nessie-delete:
	kubectl delete -f minikube/service/nessie.yaml
	kubectl delete -f minikube/deployment/nessie.yaml

postgres-up:
	kubectl create -f minikube/persistent-volume-claim/postgres.yaml
	kubectl create -f minikube/persistent-volume/postgres.yaml
	kubectl create -f minikube/service/postgres.yaml
	kubectl create -f minikube/deployment/postgres.yaml
	kubectl create -f minikube/service/pgadmin.yaml
	kubectl create -f minikube/deployment/pgadmin.yaml
postgres-update:
	kubectl apply -f minikube/service/postgres.yaml
	kubectl apply -f minikube/service/pgadmin.yaml
	kubectl apply -f minikube/deployment/postgres.yaml
	kubectl apply -f minikube/deployment/pgadmin.yaml

postgres-delete:
	kubectl delete -f minikube/service/postgres.yaml
	kubectl delete -f minikube/deployment/postgres.yaml
	kubectl delete -f minikube/service/pgadmin.yaml
	kubectl delete -f minikube/deployment/pgadmin.yaml
