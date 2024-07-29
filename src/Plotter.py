import matplotlib.pyplot as plt

def read_samples(file_path):
    """Reads decimal samples from a file and returns them as a list of floats."""
    with open(file_path, 'r') as file:
        samples = [float(line.strip()) for line in file]
    return samples

def plot_samples(samples, title):
    """Plots samples using their indices as x-coordinates and their values as y-coordinates."""
    plt.figure(figsize=(12, 6))
    plt.plot(range(len(samples)), samples, marker='o', linestyle='-', color='b')
    plt.title(title)
    plt.xlabel('Index')
    plt.ylabel('Value')
    plt.ylim(0, 1)  # Assuming all values are between 0 and 1
    plt.grid(True)
    plt.show()

if __name__ == "__main__":
    cpu_seq = read_samples('../Data/CPU_sequentiell_mac.txt')
    cpu_par = read_samples('../Data/CPU_parallel_mac.txt')
    cuda_par = read_samples('../Data/CUDA_FFT.txt')

    plot_samples(cpu_seq, "CPU Sequentiell")
    plot_samples(cpu_par, "CPU Parallel")
    plot_samples(cuda_par, "CUDA Parallel")

